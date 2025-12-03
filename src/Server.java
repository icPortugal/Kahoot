import java.io.*;
import java.net.*;
import java.util.List;

public class Server {
    public static final int PORT = 12025;
    private GameState gameState;

    public void runServer() {
        List<Question> questions = QuestionLoader.loadFromFile("perguntas.json");
        this.gameState = new GameState(questions,4);

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Servidor IsKahoot a correr na porta " + PORT);
            while(true) {
                Socket connection = server.accept();
                System.out.println("Cliente ligado: " + connection.getInetAddress());

                ClientHandler handler = new ClientHandler(connection, gameState);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }
}
