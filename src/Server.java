import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class Server {
    public static final int PORT = 12025;
    private GameManager gameManager;
    private boolean isRunning = true;

    public void runServer() {
        List<Question> questions = QuestionLoader.loadFromFile("perguntas.json");
        this.gameManager = new GameManager(questions);
        new Thread(this::runTUI).start();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Servidor IsKahoot a correr na porta " + PORT);
            while(isRunning) {
                Socket connection = server.accept();
                System.out.println("Cliente ligado: " + connection.getInetAddress());

                ClientHandler handler = new ClientHandler(connection, gameManager);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTUI() {
        Scanner scanner = new Scanner(System.in);
        while (isRunning) {
            String line = scanner.nextLine();
            String[] parts = line.trim().toLowerCase().split("\\s+");

            if (parts.length == 0 || parts[0].isEmpty()) {
                continue;
            }

            try {
                switch (parts[0]) {
                    case "new":
                        if (parts.length == 3) {
                            int numTeams = Integer.parseInt(parts[1]);
                            int numPlayers = Integer.parseInt(parts[2]);
                            gameManager.createNewGame(numTeams, numPlayers);
                        } else {
                            System.out.println("Use: new <teams> <players>");
                        }
                        break;
                    case "exit":
                        System.out.println("A encerrar o servidor...");
                        stopServer();
                        break;
                    default:
                        System.out.println("Comando desconhecido. Comandos disponíveis: new, exit");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: Os argumentos devem ser números inteiros.");
            }
        }
    }

    private void stopServer() {
        this.isRunning = false;

        if (gameManager != null) {
            gameManager.shutdown();
        }

        try {
            // fechar o ServerSocket
            new Socket("localhost", PORT).close();
        } catch (IOException e) { }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }
}
