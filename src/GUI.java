import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GUI extends JFrame {
    private GameState gameState;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private JLabel scoreLabel;
    private JLabel timeLabel;

    public GUI(GameState gameState) {
        this.gameState = gameState;

        //janela
        setTitle("Kahoot");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //no futuro ver se não faz sentido:HIDE_ON_CLOSE
        setLocationRelativeTo(null);//centrar


        //parte da pergunta
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        add(questionLabel, BorderLayout.NORTH);

        //parte das opções
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup(); //para só dar para marcar um de cada vez
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 15));
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]); //para aparecerem todas as opções
        }

        add(optionsPanel, BorderLayout.CENTER);



        JPanel bottomPanel = new JPanel(new BorderLayout());

        //pontuação + tempo
        JPanel scoreAndTime = new JPanel(new GridLayout(1, 2));
        scoreLabel = new JLabel("Pontuação: 0", SwingConstants.CENTER);
        timeLabel = new JLabel("Tempo: ", SwingConstants.CENTER);
        scoreAndTime.add(scoreLabel);
        scoreAndTime.add(timeLabel);
        bottomPanel.add(scoreAndTime, BorderLayout.NORTH);
        scoreAndTime.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        //botão submit
        //JPanel submit = new JPanel(new BorderLayout());
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 15));
        submitButton.addActionListener(this::onSubmit);
        bottomPanel.add(submitButton, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadCurrentQuestion();

        setVisible(true);

    }

    private void loadCurrentQuestion() {
        Question question = gameState.getCurrentQuestion();
        if(question == null) {
            questionLabel.setText("Sem perguntas.");
            submitButton.setEnabled(false);
            return;
        }

        // mostrar opções no ecrã
        questionLabel.setText(question.getQuestion());
        List<String> opts = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            if (i < opts.size()) {
                options[i].setText(opts.get(i));
                options[i].setVisible(true);
            } else {
                options[i].setText("");
                options[i].setVisible(false);
            }
        }
        optionsGroup.clearSelection();
    }

    private void onSubmit(ActionEvent e) {
        int selected = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].isVisible() && options[i].isSelected()) {
                selected = i; break;
            }
        }

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Por favor selecione uma opção antes de submeter.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int gained = gameState.submitAnswer(selected);
        if (gained > 0) {
            JOptionPane.showMessageDialog(this, "Correto! +" + gained + " pontos.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Errado. 0 pontos.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
        }
        scoreLabel.setText("Pontuação: " + gameState.getScore());

        if (gameState.hasNext()) {
            loadCurrentQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "Fim do jogo! Pontuação final: " + gameState.getScore(), "Fim", JOptionPane.PLAIN_MESSAGE);
            submitButton.setEnabled(false);
        }
    }

}
