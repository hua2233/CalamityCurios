package hua223.calamity.util.delaytask;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.MinecraftServer;
import oshi.annotation.concurrent.NotThreadSafe;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

@NotThreadSafe
public final class DelayRunnable {
    private static boolean INTEGRATED_SERVER;
    private static final List<DelayTask> TASK_TABLE = new ObjectArrayList<>();

    private DelayRunnable() {
    }

    public static boolean isLocalServerEnvironment() {
        return INTEGRATED_SERVER;
    }

    public static void addRunTask(int runTick, Runnable task) {
        TASK_TABLE.add(new DelayTask(runTick, task));
    }

    public static void setDist(MinecraftServer server) {
        INTEGRATED_SERVER = server.isSingleplayer();
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
        if (matchFirst(id) == null) {
            TASK_TABLE.add(new ConditionsLoopTask(loopInterval, conditions).setFlag(id));
            return true;
        }

        return false;
    }

    public static void addOrReset(int runTick, Object id, Runnable task) {
        DelayTask delayTask = matchFirst(id);
        if (delayTask != null) delayTask.tick = 0;
        else TASK_TABLE.add(new DelayTask(runTick, task).setFlag(id));
    }

    public static void setTaskTime(Object id, int tick) {
        DelayTask delayTask = matchFirst(id);
        if (delayTask != null) delayTask.tick = tick;
    }

    public static DelayTask matchFirst(Object id) {
        for (DelayTask task : TASK_TABLE)
            if (task.matchFlag(id)) return task;

        return null;
    }

    public static void removeTask(Object id) {
        DelayTask task = matchFirst(id);
        if (task != null) task.deprecated = true;
    }

    public static void conditionsLoop(BooleanSupplier conditions, int loopInterval) {
        TASK_TABLE.add(new ConditionsLoopTask(loopInterval, conditions));
    }

    public static void onTick(boolean isServer) {
        if (!TASK_TABLE.isEmpty() && (isServer || !INTEGRATED_SERVER))
            TASK_TABLE.removeIf(task -> task.deprecated || task.tryExecute());
    }
}

