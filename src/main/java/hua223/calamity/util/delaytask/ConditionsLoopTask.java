package hua223.calamity.util.delaytask;

import java.util.function.BooleanSupplier;

public class ConditionsLoopTask extends DelayTask {
    private final BooleanSupplier task;

    public ConditionsLoopTask(int runTick, BooleanSupplier task) {
        super(runTick, null);
        this.task = task;
    }

    @Override
    public boolean execute() {
        if (task.getAsBoolean()) return true;

        tick = 0;
        return false;
    }
}
