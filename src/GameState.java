import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private final List<Question> questions;
    private int currentIndex = 0;
    private Map<String, String> players = new ConcurrentHashMap<>();
    private Map<String, Integer> teamScores = new ConcurrentHashMap<>();

    public GameState(List<Question> questions) {
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

    public synchronized int submitAnswer(String teamId, int selectedIntex) {
        Question question = getCurrentQuestion();
        if(question == null) return 0;

        int pointsGained = 0;
        if(question.isCorrect(selectedIntex)) {
            pointsGained = question.getPoints();
            teamScores.merge(teamId, pointsGained, Integer::sum);
        }
            currentIndex++; //APAGAR NO PONTO 6

        return pointsGained;
    }


    public synchronized boolean hasNext() { return currentIndex < questions.size() - 1; }

}
