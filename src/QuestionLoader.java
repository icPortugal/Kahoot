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

            Quiz quiz = gson.fromJson(jsonReader, Quiz.class);

            if (quiz == null || quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
                System.err.println("Nenhuma perguntada encontrada.");
                return new ArrayList<>();
            }

            System.out.println("Quiz: " + quiz.getName());
            return quiz.getQuestions();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
