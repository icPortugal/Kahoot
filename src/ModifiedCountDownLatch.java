import java.util.concurrent.CountDownLatch;
public class ModifiedCountDownLatch {
    private int count;
    private final int initialCount;
    private int round = 0;

    private final int bonusFactor;
    private final int bonusCount;
    private final int waitPeriod;

    public ModifiedCountDownLatch(int bonusFactor, int bonusCount, int waitPeriod, int count) {
        this.count = count;
        this.initialCount = count;
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
    }

    public synchronized int getCount() {
        return count;
    }

    public synchronized void await () throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeToWait = waitPeriod;
        int arrivedRound = this.round;

        while(count > 0 && timeToWait > 0 && this.round == arrivedRound) {
            try {
                wait(timeToWait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            timeToWait = waitPeriod - elapsedTime;
        }
    }

    public synchronized int countDown() {
        int fator = 1;
        int alreadyResponded = initialCount - count;
        if (alreadyResponded < bonusCount)
            fator = bonusFactor;
        count--;
        if (count <= 0) {
            notifyAll();
        }
        return fator;
    }

    public synchronized void reset(){
        count = initialCount;
        round++;
        notifyAll();
    }
}
