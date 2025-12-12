import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {

    private final List<Question> questions;
    private int currentIndex = 0;

    private Map<String, String> players = new ConcurrentHashMap<>();
    private Map<String, Integer> teamScores = new ConcurrentHashMap<>();
    private Map<String, Integer> roundScores = new ConcurrentHashMap<>();

    private final int NUM_PLAYERS;
    private ModifiedCountDownLatch latch; //para perguntas individuais

    // para perguntas em grupo
    private Map<String, TeamBarrier> teamBarriers = new ConcurrentHashMap<>();
    private final int NUM_PLAYERS_PER_TEAM = 2;
    private int playersFinished = 0;
    private Map<String, Boolean> processedTeamRounds = new ConcurrentHashMap<>(); // para só um jogador de cada equipa conseguir registar o valor da ronda anterior

    private final List<ClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();

    public GameState(List<Question> questions, int numPlayers) {
        this.NUM_PLAYERS = numPlayers;
        this.latch = new ModifiedCountDownLatch(2,2,30000, NUM_PLAYERS);
        this.questions = questions;
    }

    public synchronized boolean registerPlayer(String username, String teamId, ClientHandler handler) {
        if (players.containsKey(username) || players.size() >= NUM_PLAYERS) {
            return false;
        }

        players.put(username, teamId);
        teamScores.putIfAbsent(teamId, 0);
        connectedHandlers.add(handler);
        return true;
    }

    public void sendScoreBoardBroadcast() {
        Map<String, Integer> totalScores = getScoreboard();
        Map<String, Integer> roundScores = getRoundScores();
        Object[] scoreboardData = new Object[] {totalScores, roundScores};

        for (ClientHandler handler : connectedHandlers) {
            try {
                handler.sendObject(scoreboardData);
            } catch (IOException e) {
                System.err.println("Erro ao enviar placar para cliente " + handler.getName());
                connectedHandlers.remove(handler);
            }
        }
    }

    public synchronized int getTeamScore(String teamId) {
        return teamScores.getOrDefault(teamId, 0);
    }
    public synchronized Question getCurrentQuestion() { // para não haver misturas de índices entre threads
        if(currentIndex >= 0 && currentIndex < questions.size()) return questions.get(currentIndex);
        return null;
    }

    public synchronized Map<String, Integer> getRoundScores() {
        return new ConcurrentHashMap<>(roundScores);
    }

    public synchronized boolean registerPlayer(String username, String teamId) {
        if (players.containsKey(username) || players.size() >= NUM_PLAYERS) {
            return false;
        }
        players.put(username, teamId);
        teamScores.putIfAbsent(teamId, 0); // inicializa a pontuação da equipa
        return true;
    }

    private synchronized void updateScores(String teamId, int pointsGained) {
        teamScores.merge(teamId, pointsGained, Integer::sum);
        roundScores.merge(teamId, pointsGained, Integer::sum);
    }

    public int submitAnswer(String teamId, int selectedIndex) {
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) return 0;

        int currentIndex = this.currentIndex; // para usar na barreira
        boolean isIndividualQuestion = (currentIndex % 2 == 0);
        if(isIndividualQuestion) {
            return submitIndividualAnswer(teamId, selectedIndex, currentQuestion,currentIndex);
        }else{
            return submitTeamAnswer(teamId, selectedIndex, currentQuestion, currentIndex);
        }
    }

    private int submitIndividualAnswer(String teamId, int selectedIndex, Question currentQuestion, int currentIndex) {
        int speedFactor = latch.countDown();
        int pointsGained = 0;

        if(currentQuestion.isCorrect(selectedIndex)) {
            pointsGained = currentQuestion.getPoints() * speedFactor;
        }

        synchronized(this) {
            updateScores(teamId, pointsGained);
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

        // para não dar para jogadores da mesma equipa registarem pontuações para rondas diferentes
        String roundKey = teamId + "_" + currentIndex;
        if(processedTeamRounds.putIfAbsent(roundKey, true) == null) {
            synchronized (this) {
                updateScores(teamId, teamPoints);
            }
        }

        return teamPoints;
    }

    public synchronized void waitForNextQuestion() {
        playersFinished++;

        if (playersFinished == NUM_PLAYERS) {
            sendScoreBoardBroadcast();

            currentIndex++;
            playersFinished = 0;
            latch.reset();
            processedTeamRounds.clear();
            roundScores.clear();

            notifyAll();

        } else {
            try {
                wait(); // fica a dormir DEPOIS de ter recebido os pontos na GUI
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public synchronized Map<String, Integer> getScoreboard() {
        return new ConcurrentHashMap<>(teamScores);
    }

    public synchronized boolean hasNext() { return currentIndex < questions.size(); }

    // bloqueia a thread até o currentIndex atingir o fim da lista de perguntas
    public void runGameLoop() {
        while(hasNext()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        }
    }

    public synchronized boolean isRoundCoordinationFinished() {
        return latch.getCount() <= 0;
    }
}
