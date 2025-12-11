import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class GUI extends JFrame {

    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private final String username;
    private final String teamId;

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JLabel timeLabel;
    private JTable scoreTable;
    private DefaultTableModel tableModel;

    private boolean gameEnded = false;

    private Timer timer;
    private int remainingTime;

    public GUI(String serverAddress, int port, String gameCode, String username, String teamId) throws IOException {

        Socket socket = new Socket(serverAddress, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.username = username;
        this.teamId = teamId;

        try {
            this.out.writeObject(new String[]{gameCode, this.username, this.teamId});
            this.out.flush();
        } catch (IOException e) {
            throw new IOException("Falha no envio do login inicial: " + e.getMessage());
        }

        setTitle("Kahoot: " + teamId + " - " + username);
        setSize(700, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());

        // Pergunta no topo do painel esquerdo
        questionLabel = new JLabel("A aguardar...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        leftPanel.add(questionLabel, BorderLayout.NORTH);

        // Opções
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            int index = i;
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 15));
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]);
            options[i].addActionListener(e -> onOptionSelected(index));
        }
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leftPanel.add(optionsPanel, BorderLayout.CENTER);

        // Tempo
        timeLabel = new JLabel("Tempo: ", SwingConstants.CENTER);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        leftPanel.add(timeLabel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.CENTER);

        //Cronometro
        timer = new Timer(1000, (ActionEvent e) -> {
            remainingTime--;
            timeLabel.setText("Tempo: " + remainingTime + " s");
            if (remainingTime <= 0) {
                timer.stop();
                onOptionSelected(-1); // Indica que o tempo esgotou sem resposta
            }
        });


        // Tabela de Pontuações
        String[] columnNames = {"Team", "Score", "Last Round Score"};
        tableModel = new DefaultTableModel(columnNames, 0);
        scoreTable = new JTable(tableModel);
        scoreTable.setEnabled(false); // Apenas leitura

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setPreferredSize(new Dimension(200, 0)); // Largura da tabela

        add(scrollPane, BorderLayout.EAST);

        new Thread(new ServerListener()).start();

        setVisible(true);
    }
    
    private void loadQuestion(Question question) {
        if(question == null) {
            questionLabel.setText("Sem perguntas.");
            for(JRadioButton opt : options) opt.setEnabled(false);
            return;
        }

        questionLabel.setText(question.getQuestion());
        List<String> opts = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            if (i < opts.size()) {
                options[i].setText(opts.get(i));
                options[i].setVisible(true);
                options[i].setEnabled(true); // Reativa os botões
            } else {
                options[i].setVisible(false);
                options[i].setEnabled(false);
            }
        }
        optionsGroup.clearSelection();
        remainingTime = 15;
        timeLabel.setText("Tempo: " + remainingTime + " s");
        timer.restart();
    }

    private void updateScoreboard(Map<String, Integer> totalScores, Map<String, Integer> roundScore) {
        tableModel.setRowCount(0); // Limpa tabela

        for (Map.Entry<String, Integer> entry : totalScores.entrySet()) {
            String team = entry.getKey();
            int totalScore = entry.getValue();
            int round = roundScore.getOrDefault(team, 0);

            tableModel.addRow(new Object[]{team, totalScore, round});
        }
    }

    private void processObject(Object obj) {
        if (obj instanceof Question) {
            // Se for um Objeto Question, carrega a pergunta no ecrã.
            loadQuestion((Question) obj);
        } else if (obj instanceof Object[]) {
            // Se receber um Object[], atualiza a tabela
            Object[] scores = (Object[]) obj;
            if (scores.length == 2 && scores[0] instanceof Map && scores[1] instanceof Map) {
                Map<String, Integer> totalScores = (Map<String, Integer>) scores[0];
                Map<String, Integer> roundScores = (Map<String, Integer>) scores[1];

                updateScoreboard(totalScores, roundScores);
            } else {
                System.err.println("Formato de pontuação inválido para o placar.");
            }
        }
        else if (obj instanceof String) {
            String command = (String) obj;
            if (command.startsWith("GAME_OVER")) {
                if (timer.isRunning()) timer.stop();
                questionLabel.setText("FIM DO JOGO.");
                for(JRadioButton opt : options) opt.setEnabled(false);
                String whithoutGameOver = command.substring(10); //10=tamanho de "GAME_OVER:"
                JOptionPane.showMessageDialog(this, whithoutGameOver);
            } else if (command.startsWith("ERRO:")) {
                JOptionPane.showMessageDialog(this, command, "ERRO", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onOptionSelected(int selected) {
        if (timer.isRunning()) {
            timer.stop();
        }
        for (JRadioButton opt : options) {
            opt.setEnabled(false); // Desativa enquanto espera pelo resultado
        }
        try {
            this.out.writeObject(Integer.valueOf(selected));
            this.out.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Conexão perdida ao enviar resposta.", "Erro de Rede", JOptionPane.ERROR_MESSAGE);
        }
    }


private class ServerListener implements Runnable { //poderia ser extends thread, mas assim fica mais claro que é uma thread interna

        @Override
        public void run() {
            try {
                Object receivedObject;
                while (true) {
                    receivedObject = in.readObject(); //vê se recebeu algo do servidor
                    if (receivedObject == null) break;

                    if (receivedObject instanceof String) {
                        String text = (String) receivedObject;
                        if (text.startsWith("GAME_OVER")) {
                            gameEnded = true;
                        }
                    }

                    final Object finalObject = receivedObject;
                    SwingUtilities.invokeLater(() -> GUI.this.processObject(finalObject)); //manda para a GUI lidar com o objeto
                }
            } catch (EOFException e) {
                // Conexão fechada pelo servidor
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro irrecuperável na Thread de Leitura: " + e.getMessage());
            } finally {
                if (!gameEnded) { // Só mostra erro se o jogo não tiver acabado normalmente
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(GUI.this,
                                "Conexão perdida com o servidor.",
                                "Erro de Conexão",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    for (JRadioButton opt : options) opt.setEnabled(false);
                });
            }
        }
    }
}
