import java.util.List;

public class GameState {

    private final List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;


    public GameState(List<Question> questions) {
        this.questions = questions;
    }

    public synchronized Question getCurrentQuestion() {
        if(currentIndex >= 0 && currentIndex < questions.size()) return questions.get(currentIndex);
        return null;
    }

    public synchronized int submitAnswer(int selectedIntex) {
        Question question = getCurrentQuestion();
        if(question == null) return 0;

        int pointsGained = 0;
        if(question.isCorrect(selectedIntex)) {
            pointsGained = question.getPoints();
            score += pointsGained;
        }

        return pointsGained;
    }

    public synchronized int getScore() { return score; }

    public synchronized boolean hasNext() { return currentIndex < questions.size() - 1; }
    public synchronized int getNumberOfQuestions() { return questions.size(); }
    public synchronized int getCurrentQuestionNumber() { return currentIndex + 1; }

}
