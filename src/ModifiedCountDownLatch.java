import java.util.concurrent.CountDownLatch;
public class ModifiedCountDownLatch { //desbloqueia uma thread quando o contador chega a zero
    private int count; //nº de respostas esperadas = nº threads a esperar
    private final int initialCount;
    private int round = 0; //por causa do reset que pode ser chamado enquanto threads estão à espera

    private final int bonusFactor;
    private final int bonusCount;
    private final int waitPeriod;

    public ModifiedCountDownLatch(int bonusFactor, int bonusCount, int waitPeriod, int count) {
        this.count = count;
        this.initialCount = count; // para o reset
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount; //nº de respostas que recebem bónus
        this.waitPeriod = waitPeriod;
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
        if (count <= 0)
            notifyAll();
        return fator; //quem respondeu primeiro tem mais pontos
    }

    public synchronized void reset(){
        count = initialCount;
        round++;
        notifyAll();
    }
}
