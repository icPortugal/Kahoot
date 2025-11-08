import java.util.List;

public class Question {
    private String question;
    private int points;
    private int correct;
    private List<String> options;

    public Question() {}

    public String getQuestion() {
        return question;
    }

    public int getPoints() {
        return points;
    }

    public int getCorrectIndex() {
        return Math.max(0, correct - 1);
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == getCorrectIndex();
    }

}
