import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private final List<Question> questions;
    private int currentIndex = 0;

    private Map<String, String> players = new ConcurrentHashMap<>();
    private Map<String, Integer> teamScores = new ConcurrentHashMap<>();

    private final int NUM_PLAYERS;
    private ModifiedCountDownLatch latch; //para perguntas individuais

    //para perguntas em grupo
    private Map<String, TeamBarrier> teamBarriers = new ConcurrentHashMap<>();
    private final int NUM_PLAYERS_PER_TEAM = 2;
    private int playersFinished = 0;
    private Map<String, Boolean> processedTeamRounds = new ConcurrentHashMap<>(); //para só um jogador de cada equipa conseguir registar o valor da ronda anterior

    public GameState(List<Question> questions, int numPlayers) {
        this.NUM_PLAYERS = numPlayers;
        this.latch = new ModifiedCountDownLatch(2,2,30000, NUM_PLAYERS);
        this.questions = questions;
    }

    public synchronized int getTeamScore(String teamId) {
        return teamScores.getOrDefault(teamId, 0);
    }
    public synchronized Question getCurrentQuestion() { //para não haver misturas de indices entre threads
        if(currentIndex >= 0 && currentIndex < questions.size()) return questions.get(currentIndex);
        return null;
    }

    public synchronized boolean registerPlayer(String username, String teamId) {
        if (players.containsKey(username) || players.size() >= NUM_PLAYERS) {
            return false;
        }
        players.put(username, teamId);
        teamScores.putIfAbsent(teamId, 0); // Inicializa a pontuação da equipa
        return true;
    }

    public int submitAnswer(String teamId, int selectedIndex) {
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) return 0;

        int currentIndex = this.currentIndex; //para usar na barreira
        boolean isIndividualQuestion = (currentIndex % 2 == 0); // perguntas individuais são as de índice par
        if(isIndividualQuestion) {
            return submitIndividualAnswer(teamId, selectedIndex, currentQuestion,currentIndex);
        }else{
            return submitTeamAnswer(teamId, selectedIndex, currentQuestion, currentIndex);
        }
    }

    private int submitIndividualAnswer(String teamId, int selectedIndex, Question currentQuestion, int currentIndex) {
        int speedFactor = latch.countDown();
        int pointsGained = 0;
        synchronized(this) {
            if(currentQuestion.isCorrect(selectedIndex)) {
                pointsGained = currentQuestion.getPoints() * speedFactor;
                teamScores.merge(teamId, pointsGained, Integer::sum);
            }
        }
        try{
            latch.await();
        } catch(Exception e) { e.printStackTrace();}
        return pointsGained;
    }

    private int submitTeamAnswer(String teamId, int selectedIndex, Question currentQuestion, int currentIndex) {
        teamBarriers.putIfAbsent(teamId, new TeamBarrier(NUM_PLAYERS_PER_TEAM, 15000));
        TeamBarrier barrier = teamBarriers.get(teamId);

        boolean isCorrect = currentQuestion.isCorrect(selectedIndex);
        int pointsGained = currentQuestion.getPoints();
        int teamPoints = 0;
        try {
            teamPoints = barrier.await(pointsGained, isCorrect);
        } catch (InterruptedException e) { e.printStackTrace();}

        // para nao dar para jogadores da mesma equipa registarem pontuaçoes para rondas diferentes
        String roundKey = teamId + "_" + currentIndex;
        if(processedTeamRounds.putIfAbsent(roundKey, true) == null) {
            synchronized (this) {
                teamScores.merge(teamId, teamPoints, Integer::sum);
            }
        }

        return teamPoints;
    }

    public synchronized void waitForNextQuestion() {
        playersFinished++;

        if (playersFinished < NUM_PLAYERS) {
            try {
                wait(); // fica a dormir DEPOIS de ter recebido os pontos na GUI
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            currentIndex++;
            playersFinished = 0;
            latch.reset();
            processedTeamRounds.clear();

            notifyAll();
        }
    }
    public synchronized Map<String, Integer> getScoreboard() {
        return new ConcurrentHashMap<>(teamScores);
    }

    public synchronized boolean hasNext() { return currentIndex < questions.size(); }

}
