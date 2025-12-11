public class GameRunner implements Runnable {
    private final GameState gameState;
    private final String gameCode;
    private final GameManager gameManager;

    public GameRunner(String gameCode, GameState gameState, GameManager gameManager) {
        this.gameCode = gameCode;
        this.gameState = gameState;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        System.out.println("Jogo " + gameCode + " a iniciar execução.");

        // espera até que o jogo termine
        gameState.runGameLoop();

        System.out.println("Jogo " + gameCode + " finalizado!");
        gameManager.removeGame(gameCode);
    }
}
