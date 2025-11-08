import java.util.List;

public class Question {
    private String question;
    private String answer;
    private int points;
    private List<Question> questions;
    private String type;
    public Question() {}

    public boolean isCorrect(String answer) {
        return this.answer.equals(answer);
    }

    public String getQuestion() {
        return question;
    }
    public String getAnswer() {
        return answer;
    }

    public int getPoints() {
        return points;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public String getType() {
        return type;
    }
}
