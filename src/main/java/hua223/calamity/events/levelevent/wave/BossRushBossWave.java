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
import net.minecraft.world.phys.Vec3;

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

    public void tick() {
        if (boss != null && !boss.isAlive()) {
            Entity.RemovalReason removalReason = boss.getRemovalReason();
            if (removalReason == Entity.RemovalReason.DISCARDED){
                event.attemptToGenerateTheNextWave();
                onKillDrop(null);
            } else if (removalReason != null && removalReason != Entity.RemovalReason.KILLED) event.failureEvent();
        }
    }

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
        ServerLevel level = event.level;
        boolean changeDimension = getVenueWorld() != level.dimension();
        if (changeDimension) event.restoreDefaultVenue();

        level = changeDimension ? level.getServer().getLevel(getVenueWorld()) : event.level;
        Vec3 position = changeDimension ? dimensionOrigin = getNewWorldVenueCenter(level) : event.original;
        //在维度场地发生变化时，我们才应该去传送所有玩家到新的世界原点
        if (changeDimension) {
            event.allowChangedDimension = true;
            for (ServerPlayer p : event.getAllPlayer()) {
                p.teleportTo(level, position.x, position.y, position.z, 0, 0);
                event.rePlayBGMusik(p);
            }
            event.allowChangedDimension = false;

        }

        event.setTerminusBoundingBox(level, position.x, position.z, getVenueSize());
    }
}
