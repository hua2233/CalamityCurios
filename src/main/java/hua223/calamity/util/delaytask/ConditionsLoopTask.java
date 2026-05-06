package hua223.calamity.util.delaytask;

import java.util.function.BooleanSupplier;

public class ConditionsLoopTask extends DelayTask {
    private final BooleanSupplier task;

    public ConditionsLoopTask(int runTick, BooleanSupplier task) {
        super(runTick, null);
        this.task = task;
    }

    @Override
    public boolean tryExecute() {
        if (tick++ >= runTick) {
            if (task.getAsBoolean()) return true;
            else tick = 0;
        }

        return false;
    }
}
