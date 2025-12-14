import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler extends Thread {
    private Socket connection;
    private GameState gameState;
    private GameManager gameManager;
    private String gameCode;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private String teamId;
    private String username;

    public ClientHandler(Socket connection, GameManager gameManager) {
        this.connection = connection;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            if (!handleLogin()) {
                out.writeObject("Falha no registo (username repetido, jogo inexistente ou equipa inválida).");
                return;
            }

            processGameLoop();

        } catch (EOFException e) {
            System.out.println("Cliente " + username + " desconectado.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro de I/O ou desserialização para " + username + ": " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private boolean handleLogin() throws IOException, ClassNotFoundException {
        Object received = in.readObject();

        if (!(received instanceof String[]) || ((String[]) received).length < 3) {
            out.writeObject("Formato de login inválido.");
            return false;
        }

        String[] parts = (String[]) received;
        this.gameCode = parts[0].trim();
        this.username = parts[1].trim();
        this.teamId = parts[2].trim();

        this.gameState = gameManager.getGame(this.gameCode);
        if (this.gameState == null) {
            System.out.println("Cliente " + username + " rejeitado: Jogo " + gameCode + " não existe.");
            return false;
        }

        // servidor rejeita se o nome já estiver a ser usado
        if (gameState.registerPlayer(this.username, this.teamId, this)) {
            // envia a primeira pergunta para iniciar o jogo (o cliente está à espera)
            out.writeObject(gameState.getCurrentQuestion());
            return true;
        } else {
            return false;
        }
    }

    private void processGameLoop() throws IOException, ClassNotFoundException {

        while (true) {
            Object response = in.readObject();

            if (response instanceof Integer) {
                int selectedIndex = (int) response;
                gameState.submitAnswer(this.teamId, selectedIndex);

                Map<String, Integer> totalScores = gameState.getScoreboard();
                Map<String, Integer> roundScores = gameState.getRoundScores();

                Object[] scoreUpdate = new Object[]{totalScores, roundScores};

                // Enviar atualização de pontuação para as GUIs
                out.writeObject(scoreUpdate);
                out.flush(); //envia logo o que está no buffer
                out.reset();

                gameState.waitForNextQuestion();

                if (gameState.hasNext()) {
                    out.writeObject(gameState.getCurrentQuestion());
                    out.flush();
                    out.reset();
                } else {
                    int pontuacaoFinal = gameState.getTeamScore(this.teamId);
                    out.writeObject("GAME_OVER: Jogo concluido: A tua equipa conseguiu " + pontuacaoFinal + " pontos.");
                    out.flush();
                    break;
                }
            } else {
                out.writeObject("ERROR: Resposta inválida.");
            }
        }
    }

    public void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (connection != null) connection.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão para " + username);
        }
    }

    public synchronized void sendObject(Object obj) throws  IOException {
        out.writeObject(obj);
    }
}
