import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Uso: java Main <IP> <PORT> <Jogo> <Equipa> <Username>");
            System.exit(1);
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);
        String gameCode = args[2];
        String teamId = args[3];
        String username = args[4];

        try {
            long delay = (long) (Math.random() * 500);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            new GUI(serverAddress, port, gameCode, username, teamId);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor em " + serverAddress + ":" + port);
            System.err.println("Detalhes: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Erro: A porta deve ser um número válido.");
            System.exit(1);
        }

    }
}
