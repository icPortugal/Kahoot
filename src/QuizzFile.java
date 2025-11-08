import java.util.List;


class QuizFile {
    private List<Quiz> quizzes;
    public List<Quiz> getQuizzes() { return quizzes; }
}


class Quiz {
    private String name;
    private List<Question> questions;
    public String getName() { return name; }
    public List<Question> getQuestions() { return questions; }
}