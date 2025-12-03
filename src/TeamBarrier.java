import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TeamBarrier {
    private final int totalPlayers;
    private final long timeoutMillis;

    private int count; // quantos ainda não chegaram
    private int round = 0;

    private List<Integer> currentAnswers; // respostas da equipa na ronda atual
    private List<Boolean> currentCorrectness; // se as respostas estão corretas

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
                long timeout= TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
                while (count > 0 && currentRound == this.round && timeout > 0) {
                    timeout = allArrived.awaitNanos(timeout); //assim mesmo que algum jogador vá a baixo nao ficam todos á espera
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
                allCorrect = false; // exemplo: cada resposta correta vale 10 pontos
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
