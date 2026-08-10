package hua223.calamity.events.levelevent;

import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class LevelEvent <T extends ILevelWave> {
    private static LevelEvent<?> activeWorldEvent;
    private T currentWave;
    private boolean inProgress;
    public ServerLevel level;
    protected List<T> challengeWave;
    protected final IDataPackResponse response;
    private int tick;

    public LevelEvent(ServerPlayer player, IDataPackResponse response) {
        if (activeWorldEvent != null) activeWorldEvent.endEvent();
        activeWorldEvent = this;
        this.response = response;
        level = player.serverLevel();
    }

    public void start() {
        inProgress = true;
    }

    protected void pre() {
        challengeWave = new ArrayList<>();
        loadSpawnWave(challengeWave);
        challengeWave.sort(ILevelWave::compareTo);
        MinecraftForge.EVENT_BUS.register(this);
        DelayRunnable.addUniqueLoopTask(() -> {
            this.tick(tick);
            tick++;
            return !inProgress();
        }, 1, this);
    }

    protected void tick(int tickCount) {

    }


    public static boolean inProgress(Class<? extends LevelEvent<?>> event) {
        return activeWorldEvent != null && activeWorldEvent.getClass() == event;
    }

    public static LevelEvent<?> getActiveWorldEvent() {
        return activeWorldEvent;
    }

    public T getCurrentWave() {
        return currentWave;
    }

    protected final T getNetWave() {
        return currentWave = challengeWave.remove(challengeWave.size() - 1);
    }

    protected List<ServerPlayer> getAllPlayer() {
        return level.getServer().getPlayerList().getPlayers();
    }

    @SubscribeEvent
    public void onDrop(LivingDropsEvent event) {
        if (currentWave.isMobInWave(event.getEntity())) {
            attemptToGenerateTheNextWave();
            currentWave.onKillDrop(event);
        }
    }

    public boolean inProgress() {
        return inProgress;
    }

    protected abstract void attemptToGenerateTheNextWave();

    public void interruptEvent() {
        if (level != null) endEvent();
        response.getPack().putInt("state", 1);
        response.sendToAllClient();
    }

    protected void failureEvent() {
        endEvent();
        response.getPack().putInt("state", 2);
        response.sendToAllClient();
    }

    protected void victoryEvent() {
        endEvent();
        for (ServerPlayer player :  LevelEvent.this.getAllPlayer()) {
            response.getPack().putInt("state", 3);
            response.sendToClient(player);
        }
    }

    protected void endEvent() {
        activeWorldEvent = null;
        if (inProgress) {
            MinecraftForge.EVENT_BUS.unregister(this);
            inProgress = false;
            if (currentWave != null)
                currentWave.onTerminationEvent();
        }
    }

    protected abstract void loadSpawnWave(List<T> list);
}
