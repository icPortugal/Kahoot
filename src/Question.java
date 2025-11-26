import java.util.List;
import java.io.Serializable;
public class Question implements Serializable {
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
        return correct;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == getCorrectIndex();
    }

}
