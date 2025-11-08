import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Question> perguntas = QuestionLoader.loadFromFile("perguntas.json");
        GameState gs = new GameState(perguntas);
        new GUI(gs);
    }
}