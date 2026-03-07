package hua223.calamity.util.delaytask;

public class DelayTask {
    final int runTick;
    protected Runnable task;
    protected int tick;
    private boolean invalid;
    protected Object flag;

    public DelayTask(int runTick, Runnable task) {
        this.runTick = runTick;
        this.task = task;
    }

    public boolean canRun() {
        return tick++ >= runTick;
    }

    public final boolean isInvalid() {
        return invalid;
    }

    public final void invalidation() {
        invalid = true;
    }

    public boolean execute() {
        task.run();
        return true;
    }

    public DelayTask setFlag(Object o) {
        if (flag == null && o != null) flag = o;
        return this;
    }

    public boolean matchFlag(Object id) {
        return id == flag;
    }
}
