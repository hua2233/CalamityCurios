package hua223.calamity.util.delaytask;

public class LoopTask extends DelayTask {
    private int loopCount;

    public LoopTask(int loopInterval, int loopCount, Runnable task) {
        super(loopInterval, task);
        this.loopCount = loopCount;
    }

    @Override
    public boolean tryExecute() {
        if (tick++ >= runTick) {
            task.run();
            tick = 0;
            return --loopCount <= 0;
        }

        return false;
    }
}
