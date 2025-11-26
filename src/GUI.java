import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class GUI extends JFrame {

    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final String username;
    private final String teamId;

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JLabel scoreLabel;
    private JLabel timeLabel;

    public GUI(String serverAddress, int port, String username, String teamId) throws IOException {

        Socket socket = new Socket(serverAddress, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.username = username;
        this.teamId = teamId;

        try {
            this.out.writeObject(new String[]{"JOGO_1", this.username, this.teamId});
            this.out.flush();
        } catch (IOException e) {
            throw new IOException("Falha no envio do login inicial: " + e.getMessage());
        }

        setTitle("IsKahoot Client - " + username);
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            int index = i; // Variável efetivamente final
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 15));
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]);
            options[i].addActionListener(e -> onOptionSelected(index));
        }
        add(optionsPanel, BorderLayout.CENTER);

        JPanel scoreAndTime = new JPanel(new GridLayout(1, 2));
        scoreLabel = new JLabel("Pontuação: 0", SwingConstants.CENTER);
        timeLabel = new JLabel("Tempo: --", SwingConstants.CENTER);
        scoreAndTime.add(scoreLabel);
        scoreAndTime.add(timeLabel);
        scoreAndTime.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(scoreAndTime, BorderLayout.SOUTH);

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
    }

    
    private void processObject(Object obj) {
        if (obj instanceof Question) {
            // Se for um Objeto Question, carrega a pergunta no ecrã.
            loadQuestion((Question) obj);
        } else if (obj instanceof String) {
            String command = (String) obj;

            if (command.startsWith("Pontuação:")) {
                String score = command.substring(command.lastIndexOf(':') + 1).trim();
                scoreLabel.setText("Pontuação: " + score);
                JOptionPane.showMessageDialog(this, command);
            } else if (command.startsWith("GAME_OVER")) {
                questionLabel.setText("FIM DO JOGO.");
                for(JRadioButton opt : options) opt.setEnabled(false);
                JOptionPane.showMessageDialog(this, command);
            } else if (command.startsWith("ERRO:")) {
                JOptionPane.showMessageDialog(this, command, "ERRO", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onOptionSelected(int selected) {
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
private class ServerListener implements Runnable {

        @Override
        public void run() {
            try {
                Object receivedObject;
                while (true) {
                    receivedObject = in.readObject();
                    if (receivedObject == null) break;

                    final Object finalObject = receivedObject;
                    SwingUtilities.invokeLater(() -> GUI.this.processObject(finalObject));
                }
            } catch (EOFException e) {
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro irrecuperável na Thread de Leitura: " + e.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(GUI.this, "Fim do jogo ou Conexão perdida. Pontuação final não garantida.", "Fim", JOptionPane.PLAIN_MESSAGE);
                    for (JRadioButton opt : options) opt.setEnabled(false);
                });
            }
        }
    }
}
