package hua223.calamity.util.delaytask;

import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public final class DelayRunnable {
    private static boolean INTEGRATED_SERVER;
    private static final List<DelayTask> TASK_TABLE = new CopyOnWriteArrayList<>();

    private DelayRunnable() {
    }

    public static void addRunTask(int runTick, Runnable task) {
        TASK_TABLE.add(new DelayTask(runTick, task));
    }

    public static void setDist(ServerStartedEvent event) {
        INTEGRATED_SERVER = event.getServer().isSingleplayer();
    }

    public static void nextTickRun(Runnable task) {
        TASK_TABLE.add(new DelayTask(1, task));
    }

    public static void currentTickEndRun(Runnable task) {
        TASK_TABLE.add(new DelayTask(0, task));
    }

    public static void loopTask(int loopCount, int loopInterval, Runnable task) {
        TASK_TABLE.add(new LoopTask(loopInterval, loopCount, task));
    }

    public static boolean addUniqueLoopTask(BooleanSupplier conditions, int loopInterval, Object id) {
        if (TASK_TABLE.stream().noneMatch(task -> task.matchFlag(id))) {
            TASK_TABLE.add(new ConditionsLoopTask(loopInterval, conditions).setFlag(id));
            return true;
        }

        return false;
    }

    public static void addOrReset(int runTick, Object id, Runnable task) {
        Optional<DelayTask> optional = TASK_TABLE.stream().filter(delayTask -> delayTask.matchFlag(id)).findFirst();
        if (optional.isPresent()) optional.get().tick = 0;
        else TASK_TABLE.add(new DelayTask(runTick, task).setFlag(id));
    }

    public static void addIterativeTask(int runTick, Object id, Runnable task) {
        TASK_TABLE.add(new IterativeTask(runTick, task, id));
    }

    public static boolean iterableIfHas(Object id) {
        DelayTask task = TASK_TABLE.stream()
            .filter(delayTask -> delayTask.matchFlag(id))
            .findFirst().orElse(null);

        if (task != null) {
            ((IterativeTask) task).trackingCount++;
            return true;
        }

        return false;
    }

    public static int getIterableCount(Object id) {
        DelayTask task = TASK_TABLE.stream()
            .filter(delayTask -> delayTask.matchFlag(id))
            .findFirst().orElse(null);

        return task == null ? -1 : ((IterativeTask) task).trackingCount;
    }

    public static void iterativeTask(Object id) {
        TASK_TABLE.stream()
            .filter(delayTask -> delayTask.matchFlag(id))
            .findFirst().ifPresent(task -> {
                IterativeTask iterative = (IterativeTask) task;
                iterative.tick = 0;
                iterative.trackingCount++;
            });
    }

    public static void setTaskTime(Object id, int tick) {
        TASK_TABLE.stream()
            .filter(delayTask -> delayTask.matchFlag(id))
            .findFirst().ifPresent(task -> task.tick = tick);
    }

    public static void removeTask(Object id) {
        TASK_TABLE.stream()
            .filter(delayTask -> delayTask.matchFlag(id))
            .findFirst().ifPresent(DelayTask::invalidation);
    }

    public static void conditionsLoop(BooleanSupplier conditions, int loopInterval) {
        TASK_TABLE.add(new ConditionsLoopTask(loopInterval, conditions));
    }

        public static void onTick(boolean isServer) {
            if (!TASK_TABLE.isEmpty() && (isServer || !INTEGRATED_SERVER))
                TASK_TABLE.removeIf(task -> task.isInvalid() || (task.canRun() && task.execute()));
        }
}

