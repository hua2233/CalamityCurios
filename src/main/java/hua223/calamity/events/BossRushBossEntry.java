package hua223.calamity.events;

import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.loots.EntitiesLootContext;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static hua223.calamity.events.BossRushEvent.getAllTestingPlayer;

public abstract class BossRushBossEntry implements Comparable<BossRushBossEntry> {
    private Mob boss;

    public abstract void spawnBoss(ServerPlayer player);

    public abstract byte getThreatLevel();

    public float getInterestValue() {
        return 0.35f;
    }

    public Mob getBoss() {
        return boss;
    }

    public int getRestTime() {
        return 60;
    }

    public void onHurt(PlayerAttackListener listener) {

    }

    public void onAttack(HurtListener listener) {

    }

    public boolean mustWait() {
        return false;
    }

    public void terminationEvent(ServerLevel lastLevel) {
        if (boss != null) boss.discard();
    }

    protected final void setEntryBossEntity(Mob mob) {
        boss = mob;
    }

    protected static UUID getBossRushUUID() {
        return UUID.nameUUIDFromBytes("BossRushEvent".getBytes());
    }

    protected static BlockPos getCenterSpawn(ServerPlayer player) {
        WorldBorder border = player.serverLevel().getWorldBorder();
        return new BlockPos((int) border.getCenterX(), player.getY() > 100 ? 1 : (int) player.getY(), (int) border.getCenterZ());
    }

    @SuppressWarnings("ConstantConditions")
    protected static void applyBoss(Mob boss, ServerPlayer player, AttributeContainer... containers) {
        UUID id = getBossRushUUID();
        for (AttributeContainer container : containers)
            boss.getAttribute(container.attribute().get()).addTransientModifier(
                new AttributeModifier(id, "BossRushEvent", container.value(), container.operation()));

        boss.setPos(player.position());
        player.level().addFreshEntity(boss);
    }

    public void onKillDrop(EntitiesLootContext context) {}

    @Override
    public int compareTo(@NotNull BossRushBossEntry entry) {
        return Integer.compare(entry.getThreatLevel(), getThreatLevel());
    }

    protected int getVenueSize() {
        return 80;
    }

    protected ResourceKey<Level> getVenueWorld() {
        return Level.OVERWORLD;
    }

    protected Vec3 getNewWorldVenueCenter(ServerPlayer player) {
        return BossRushEvent.original;
    }

    @SuppressWarnings("ConstantConditions")
    protected final void bossChange(BossRushBossEntry entry, ServerPlayer player) {
        ServerLevel currentLevel = player.serverLevel();
        List<ServerPlayer> challengers = null;
        if (entry.getVenueWorld() != getVenueWorld()) {
            challengers = BossRushEvent.getAllTestingPlayer();
            BossRushEvent.restoreDefaultVenue();
            currentLevel = currentLevel.getServer().getLevel(getVenueWorld());
            for (ServerPlayer p : challengers)
                p.changeDimension(currentLevel);
        }

        if (entry.getVenueSize() != getVenueSize() || challengers != null) {
            Vec3 position = getNewWorldVenueCenter(player);
            if (challengers == null) {
                challengers = BossRushEvent.getAllTestingPlayer();
                //仅在维度未触发修正时更改，在获取所有当前世界挑战场玩家之后恢复事件视界
                BossRushEvent.restoreDefaultVenue();
            }
            for (ServerPlayer p : challengers) {
                p.moveTo(position);
                p.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
            }

            BossRushEvent.setTerminusBoundingBox(currentLevel, position.x, position.z, getVenueSize());
            clearOtherPlayers(challengers, currentLevel);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected static void setFirstVenue(BossRushBossEntry entry, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 position = BossRushEvent.original;
        if (entry.getVenueWorld() != level.dimension()) {
            BossRushEvent.restoreDefaultVenue();
           List<ServerPlayer> list = level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(80));
           level = level.getServer().getLevel(entry.getVenueWorld());
           for (ServerPlayer p : list) {
               p.changeDimension(level);
               if (position == BossRushEvent.original)
                   position = entry.getNewWorldVenueCenter(player);
               p.teleportTo(position.x, position.y, position.z);
           }
        }

        if (level.getWorldBorder().getSize() != entry.getVenueSize() || position != BossRushEvent.original)
            BossRushEvent.setTerminusBoundingBox(level, position.x, position.z, entry.getVenueSize());
    }

    protected void clearOtherPlayers(List<ServerPlayer> challengers, ServerLevel currentLevel) {
        WorldBorder border = currentLevel.getWorldBorder();
        for (ServerPlayer player : getAllTestingPlayer()) {
            if (!challengers.contains(player) && border.isWithinBounds(player.getBoundingBox()))
                player.teleportTo(border.getMinX() - 5, player.getY(), border.getMinZ() - 5);
        }
    }
}
