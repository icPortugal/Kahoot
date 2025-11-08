import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
    private GameState gameState;
    private JFrame frame;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private JLabel scoreLabel;

    public GUI(GameState gameState) {
        this.gameState = gameState;

        //Janela
        setTitle("Kahoot");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //no futuro ver se não faz sentido:HIDE_ON_CLOSE
        setLocationRelativeTo(null);//centrar


        //parte da pergunta
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        questionLabel.setHorizontalAlignment(JTextField.RIGHT);

        //parte das opções
        JPanel optionsPanel = new JPanel();
        options =new JRadioButton[4];
        optionsGroup = new ButtonGroup(); //para só dar para marcar um de cada vez
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 15));
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]); //para aparecerem todas as opções
        }

        frame.add(optionsPanel, BorderLayout.WEST);

        //pontuação
        scoreLabel = new JLabel("Pontuação: 0", SwingConstants.CENTER);

        //enviar resposta





    }

}
