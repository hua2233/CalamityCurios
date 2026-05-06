package hua223.calamity.util.delaytask;

public class IterativeTask extends DelayTask {
    public int trackingCount;

    public IterativeTask(int runTick, Runnable task, Object flag) {
        super(runTick, task);
        this.flag = flag;
    }
}
