package hua223.calamity.events.levelevent;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ILevelWave extends Comparable<ILevelWave> {
    void spawnWave();

    int getWaveLevel();

    default void onTerminationEvent() {}

    default boolean mustWait() {
        return false;
    }

    int getRestTime();

    boolean isMobInWave(Entity entity);

    default void onKillDrop(LivingDropsEvent event) {}

    default UUID getEventUUID() {
        return UUID.nameUUIDFromBytes(getClass().getSimpleName().getBytes());
    }

    @Override
    default int compareTo(@NotNull ILevelWave wall) {
        return Integer.compare(wall.getWaveLevel(), getWaveLevel());
    }
}
