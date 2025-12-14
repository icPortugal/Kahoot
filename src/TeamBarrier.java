import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TeamBarrier {
    private final int totalPlayers;
    private final long timeoutMillis;

    private int count;
    private int round = 0;

    private List<Integer> currentAnswers;
    private List<Boolean> currentCorrectness;

    private int roundTeamScore = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition allArrived = lock.newCondition();

    public TeamBarrier(int totalPlayers, long timeoutMillis) {
        this.totalPlayers = totalPlayers;
        this.count = totalPlayers;
        this.timeoutMillis = timeoutMillis;
        this.currentAnswers = new ArrayList<>();
        this.currentCorrectness = new ArrayList<>();
    }

    public int await(int answer, boolean isCorrect) throws InterruptedException {
        lock.lock();
        try {
            int currentRound = this.round;
            currentAnswers.add(answer);
            currentCorrectness.add(isCorrect);
            count--;
            if (count == 0) {
                calculateRoundScore();
                resetForNextRound();
                return roundTeamScore;
            } else {
                long startTime = System.currentTimeMillis();
                long timeToWait = timeoutMillis;
                while (count > 0 && currentRound == this.round && timeToWait > 0) {
                    allArrived.await(timeToWait, TimeUnit.MILLISECONDS);
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    timeToWait = timeoutMillis - elapsedTime;
                }
                if (currentRound == this.round && count > 0) { // timeout ocorreu
                    calculateRoundScore(); // calcula com quem está presente
                    resetForNextRound();
                }
            }
            return roundTeamScore;
        } finally {
            lock.unlock();
        }
    }
    public void calculateRoundScore() {
        if (currentCorrectness.isEmpty()) {
            roundTeamScore = 0;
            return;
        }
        int maxScore = 0;
        boolean allCorrect = true;
        for(int i = 0; i < currentCorrectness.size(); i++) {
            if (!currentCorrectness.get(i))
                allCorrect = false;
            else {
                if (currentAnswers.get(i) > maxScore)
                    maxScore = currentAnswers.get(i);
            }
        }
        if (allCorrect) {
            roundTeamScore = maxScore * 2;
        } else  {
            roundTeamScore = maxScore;
        }
    }

    public void resetForNextRound() {
        count = totalPlayers;
        this.round ++;
        currentAnswers.clear();
        currentCorrectness.clear();
        allArrived.signalAll();
    }
}
