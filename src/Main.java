import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Question> perguntas = QuestionLoader.loadFromFile("perguntas.json");

        if(perguntas == null || perguntas.isEmpty()) {
            System.err.println("Sem perguntas. Verifique o ficheiro perguntas.json");
            System.exit(1);
        }

        GameState gs = new GameState(perguntas);
        new GUI(gs);
    }
}