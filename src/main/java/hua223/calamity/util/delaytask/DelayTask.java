package hua223.calamity.util.delaytask;

public class DelayTask {
    final int runTick;
    protected int tick;
    boolean deprecated;
    protected Object flag;
    protected Runnable task;

    public DelayTask(int runTick, Runnable task) {
        this.runTick = runTick;
        this.task = task;
    }

    public boolean tryExecute() {
        boolean run = tick++ >= runTick;
        if (run) task.run();
        return run;
    }

    public DelayTask setFlag(Object o) {
        if (flag == null && o != null) flag = o;
        return this;
    }

    public Runnable getTask() {
        return task;
    }

    public boolean matchFlag(Object id) {
        return id == flag;
    }
}
