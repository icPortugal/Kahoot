import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {
    public static List<Question> loadFromFile(String filename) {
        try (Reader jsonReader = new FileReader(filename)) {
            Gson gson = new GsonBuilder().create();

            QuizFile quizFile = gson.fromJson(jsonReader, QuizFile.class);
            if (quizFile == null || quizFile.getQuizzes() == null || quizFile.getQuizzes().isEmpty())
                return new ArrayList<>();

            Quiz selectedQuiz = quizFile.getQuizzes().get(0);
            if (selectedQuiz.getQuestions() != null)
                return selectedQuiz.getQuestions();
            else
                return new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
