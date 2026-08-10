package hua223.calamity.events.levelevent.wave;

import hua223.calamity.events.levelevent.BossRushEvent;
import hua223.calamity.events.levelevent.ILevelWave;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class BossRushBossWave implements ILevelWave {
    private Mob boss;
    private Vec3 dimensionOrigin;
    protected BossRushEvent event;

    public BossRushBossWave(BossRushEvent event) {
        this.event = event;
    }

    public float getInterestValue() {
        return 0.35f;
    }

    public void tick() {}

    public Mob getBoss() {
        return boss;
    }

    @Override
    public int getRestTime() {
        return 60;
    }

    public void onTerminationEvent() {
        if (boss != null) boss.discard();
    }

    @Override
    public boolean isMobInWave(Entity entity) {
        return entity == boss;
    }

    protected final void setEntryBossEntity(Mob mob) {
        boss = mob;
    }

    public Vec3 getCenterSpawn(int fly) {
        Vec3 o = Objects.requireNonNullElse(dimensionOrigin, event.original);
        return fly > 0 ? o.add(0, fly, 0) : o;
    }

    protected void spawnToWorld(int fly) {
        boss.setPos(getCenterSpawn(fly));
        event.level.addFreshEntity(boss);
    }

    @SuppressWarnings("ConstantConditions")
    protected void applyBossAttribute(Mob boss, AttributeContainer... containers) {
        UUID id = getEventUUID();
        for (AttributeContainer container : containers)
            boss.getAttribute(container.attribute().get()).addTransientModifier(
                new AttributeModifier(id, "BossRushEvent", container.value(), container.operation()));
    }

    protected int getVenueSize() {
        return 80;
    }

    protected ResourceKey<Level> getVenueWorld() {
        return Level.OVERWORLD;
    }

    protected Vec3 getNewWorldVenueCenter(ServerLevel level) {
        return event.original;
    }

    @SuppressWarnings("ConstantConditions")
    public final void bossChange(BossRushBossWave previousWave) {
        ServerLevel currentLevel = event.level;
        List<ServerPlayer> challengers = null;
        if (previousWave.getVenueWorld() != getVenueWorld()) {
            event.allowChangedDimension = true;
            challengers = event.getAllPlayer();
            event.restoreDefaultVenue();
            currentLevel = currentLevel.getServer().getLevel(getVenueWorld());
            for (ServerPlayer p : challengers)
                p.changeDimension(currentLevel);

            event.rePlayBGMusik();
            event.allowChangedDimension = false;
        }

        if (previousWave.getVenueSize() != getVenueSize() || challengers != null) {
            dimensionOrigin = getNewWorldVenueCenter(currentLevel);
            if (challengers == null) {
                challengers = event.getAllPlayer();
                //仅在维度未触发修正时更改，在获取所有当前世界挑战场玩家之后恢复事件视界
                event.restoreDefaultVenue();
            }
            for (ServerPlayer player : challengers) {
                player.moveTo(dimensionOrigin);
                player.connection.teleport(dimensionOrigin.x, dimensionOrigin.y, dimensionOrigin.z, player.getYRot(), player.getXRot());
            }

            event.setTerminusBoundingBox(currentLevel, dimensionOrigin.x, dimensionOrigin.z, getVenueSize());
            clearOtherPlayers(challengers, currentLevel);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public final void setFirstVenue() {
        ServerLevel level = event.level;
        Vec3 position = event.original;
        if (getVenueWorld() != level.dimension()) {
            event.restoreDefaultVenue();
            level = level.getServer().getLevel(getVenueWorld());
            for (ServerPlayer p : event.getAllPlayer()) {
                p.changeDimension(level);
                if (position == event.original)
                    position = dimensionOrigin = getNewWorldVenueCenter(level);
                p.teleportTo(position.x, position.y, position.z);
            }

            event.rePlayBGMusik();
        }

        if (level.getWorldBorder().getSize() != getVenueSize() || position != event.original)
            event.setTerminusBoundingBox(level, position.x, position.z, getVenueSize());
    }

    protected void clearOtherPlayers(List<ServerPlayer> challengers, ServerLevel currentLevel) {
        WorldBorder border = currentLevel.getWorldBorder();
        for (ServerPlayer player : event.getAllPlayer()) {
            if (!challengers.contains(player) && border.isWithinBounds(player.getBoundingBox()))
                player.teleportTo(border.getMinX() - 5, player.getY(), border.getMinZ() - 5);
        }
    }
}
