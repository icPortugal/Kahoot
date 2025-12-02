import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private final List<Question> questions;
    private int currentIndex = 0;

    private Map<String, String> players = new ConcurrentHashMap<>();
    private Map<String, Integer> teamScores = new ConcurrentHashMap<>();

    private final int NUM_PLAYERS; //PERGUNTAR------------------
    private ModifiedCountDownLatch latch;

    public GameState(List<Question> questions, int numPlayers) {
        this.NUM_PLAYERS = numPlayers;
        this.latch = new ModifiedCountDownLatch(2,2,15000, NUM_PLAYERS);
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
        if (players.containsKey(username)) {
            return false; // Username já existe
        }
        players.put(username, teamId);
        teamScores.putIfAbsent(teamId, 0); // Inicializa a pontuação da equipa
        return true;
    }

    public int submitAnswer(String teamId, int selectedIndex) {
        int questionIndex = currentIndex;
        Question question = getCurrentQuestion();
        if(question == null) return 0;

        int speedFactor = latch.countDown();

        int pointsGained = 0;
        synchronized(this) {
            if(question.isCorrect(selectedIndex)) {
                pointsGained = question.getPoints() * speedFactor;
                teamScores.merge(teamId, pointsGained, Integer::sum);
            }
        }

        try{
            latch.await();
        } catch(Exception e){
            e.printStackTrace();
        }

        synchronized (this) {
            if (questionIndex == currentIndex) { // questao basicamente só é atualiazada uma vez (pelo primeiro a responder)
                currentIndex++;
                latch.reset();
            }
        }

        return pointsGained;
    }

    public synchronized Map<String, Integer> getScoreboard() {
        return new ConcurrentHashMap<>(teamScores);
    }

    public synchronized boolean hasNext() { return currentIndex < questions.size(); }

}
