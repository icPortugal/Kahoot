import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {

    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    private final List<Question> availableQuestions;

    // gera códigos únicos para os jogos
    private final AtomicInteger gameCodeGenerator = new AtomicInteger(1);

    // threadpool para limitar a execução dos jogos
    private final ExecutorService gameThreadPool;
    private static final int MAX_CONCURRENT_GAMES = 5; // limite de 5 jogos

    public GameManager(List<Question> questions) {
        this.availableQuestions = questions;
        this.gameThreadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_GAMES);
    }

    // podemos colocar também o número de perguntas
    public String createNewGame(int numTeams, int numPlayers) {
        int totalPlayers = numTeams * numPlayers;
        String gameCode = String.valueOf(gameCodeGenerator.getAndIncrement());
        GameState newGame = new GameState(availableQuestions, totalPlayers);

        activeGames.put(gameCode, newGame);
        GameRunner runner = new GameRunner(gameCode, newGame, this);
        gameThreadPool.submit(runner);

        System.out.println("\n--- JOGO CRIADO ---");
        System.out.println("Código: " + gameCode);
        System.out.println("Jogadores esperados: " + totalPlayers);
        System.out.println("-------------------\n");

        return gameCode;
    }

    public GameState getGame(String gameCode) {
        return activeGames.get(gameCode);
    }

    public void removeGame(String gameCode) {
        activeGames.remove(gameCode);
    }

    public void shutdown() {
        gameThreadPool.shutdown();
        System.out.println("ThreadPool de jogos encerrada.");
    }

}
