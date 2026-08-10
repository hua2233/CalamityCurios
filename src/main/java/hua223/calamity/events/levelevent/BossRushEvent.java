package hua223.calamity.events.levelevent;

import hua223.calamity.events.levelevent.wave.BossRushBossWave;
import hua223.calamity.events.levelevent.wave.EndDragonWave;
import hua223.calamity.events.levelevent.wave.WardenBossWave;
import hua223.calamity.events.levelevent.wave.WitherBossWave;
import hua223.calamity.generators.DamageMapping;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class BossRushEvent extends LevelEvent<BossRushBossWave> {
    @DamageRequester(key = DamageMapping.GENERIC_KILL, id = "minecraft",
        msg = "boss_rush_failure", style = ChatFormatting.GOLD, zh_cn = "到此为止了")
    public static DamageSupplier supplier;

    private float maxTierWave;
    public boolean allowChangedDimension;
    private Set<UUID> players;
    public final Vec3 original;

    public BossRushEvent(ServerPlayer player, LevelEventActiveItem<BossRushEvent> item) {
        super(player, item);
        original = player.position();
    }

    @Override
    protected void pre() {
        setTerminusBoundingBox(level, original.x, original.z, 80);
        players = new ObjectOpenHashSet<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
            if (player.level() == level && level.getWorldBorder().isWithinBounds(player.getBoundingBox()))
                players.add(player.getUUID());
        super.pre();
        maxTierWave = challengeWave.size();
    }

    @Override
    protected void tick(int tickCount) {
        BossRushBossWave wave = getCurrentWave();
        Mob boss = null;
        if (wave != null && (boss = wave.getBoss()) != null) wave.tick();

        if (tickCount % 20 == 0) {
            List<ServerPlayer> players;
            if ((level.getDifficulty() == Difficulty.PEACEFUL || (players = getAllPlayer()).removeIf(ServerPlayer::isCreative)))
                failureEvent();
            else {
                WorldBorder worldBorder = level.getWorldBorder();
                Vec3 position = null;
                for (ServerPlayer player : players) {
                    if (!worldBorder.isWithinBounds(player.getBoundingBox())) {
                        if (position == null) position = getCurrentWave().getCenterSpawn(0);
                        player.teleportTo(position.x, position.y, position.z);
                    }
                }

                if (boss != null && !worldBorder.isWithinBounds(boss.getBoundingBox())) {
                    if (position == null) position = getCurrentWave().getCenterSpawn(0);
                    boss.teleportTo(position.x, position.y, position.z);
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        int[] count = {0};
        DelayRunnable.conditionsLoop(() -> {
            if (++count[0] == 1) pre();
            else if (count[0] == 48) attemptToGenerateTheNextWave();
            return !inProgress() || count[0] == 48;
        }, 20);
        CompoundTag pack = response.getPack();
        pack.putFloat("power", 4F);
        pack.putInt("state", 0);
        response.sendToAllClient();
    }

    @Override
    protected void loadSpawnWave(List<BossRushBossWave> list) {
        Collections.addAll(list, new WardenBossWave(this), new WitherBossWave(this), new EndDragonWave(this));
    }

    public void setTerminusBoundingBox(ServerLevel level, double x, double z, double size) {
        this.level = level;
        WorldBorder border = level.getWorldBorder();
        border.calamity$BossRushBox(x, z, size);
        border.setDamageSafeZone(WorldBorder.DEFAULT_SETTINGS.getSafeZone());
        border.calamity$Shape = Shapes.box(Math.floor(border.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(border.getMinZ()),
            Math.ceil(border.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(border.getMaxZ()));
        response.writeVec3("border", new Vec3(x, z, size), true);
        response.sendToAllClient();
    }

    @Override
    public List<ServerPlayer> getAllPlayer() {
        return level.getPlayers(p -> players.contains(p.getUUID()));
    }

    @SubscribeEvent
    public final void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (player.level() == level && !players.contains(player.getUUID()) && level.getWorldBorder().isWithinBounds(player.getBoundingBox()))
            player.setPos(level.getWorldBorder().getMinX() - 5, player.getY(), level.getWorldBorder().getMinZ() - 5);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onDimension(EntityTravelToDimensionEvent event) {
        if (!allowChangedDimension && (players != null && players.contains(event.getEntity().getUUID())))
            event.cancel();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void onPlayerDeath(LivingDeathEvent event) {
        if (players.remove(event.getEntity().getUUID())) {
            Player player = event.getEntity().calamity$Player;
            player.getCombatTracker().recordDamage(supplier.get(), 0f);
            if (players.isEmpty()) failureEvent();
            else {
                Mob boss = getCurrentWave().getBoss();
                if (boss != null && (boss.getTarget() == null || !boss.getTarget().isAlive()))
                    boss.setTarget(findTheNearestPlayer(boss));
            }
        }
    }

    @SubscribeEvent
    public final void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (players.remove(event.getEntity().getUUID()) && players.isEmpty()) endEvent();
    }

    public void restoreDefaultVenue() {
        WorldBorder border = level.getWorldBorder();
        border.calamity$Shape = null;
        WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
        border.calamity$BossRushBox(settings.getCenterX(), settings.getCenterZ(), settings.getSize());
        response.getPack().putString("border", "default");
        response.sendToAllClient();
    }

    @SuppressWarnings("all")
    public final Player findTheNearestPlayer(Entity entity) {
        return (Player) players.stream().map(level::getEntity).min(Comparator.comparingDouble(entity::distanceToSqr)).get();
    }

    public void rePlayBGMusik() {
        response.getPack().putString("sound", "replay");
        response.sendToAllClient();
    }

    @Override
    protected void attemptToGenerateTheNextWave() {
        BossRushBossWave previous = getCurrentWave();
        if (challengeWave.isEmpty()) {
            if (previous == null) failureEvent();
            else if (previous.mustWait())
                DelayRunnable.addRunTask(previous.getRestTime(), this::victoryEvent);
            else victoryEvent();
        } else {
            if (previous != null) {
                CompoundTag pack = response.getPack();
                pack.putFloat("interest", previous.getInterestValue());
                pack.putFloat("progress", Mth.clamp( 1 - (challengeWave.size() - 1) / maxTierWave, 0f, .75f));
                response.sendToAllClient();
            }

            BossRushBossWave bossEntry = getNetWave();
            Runnable runnable = () -> {
                if (inProgress()) {
                    if (previous != null) bossEntry.bossChange(previous);
                    else bossEntry.setFirstVenue();
                    bossEntry.spawnWave();
                }
            };

            if (previous == null) runnable.run();
            else DelayRunnable.addRunTask(previous.getRestTime(), runnable);
        }
    }

    public void siteChanges(int countdown) {
        CompoundTag pack = response.getPack();
        if (inProgress()) {
            if (countdown == 38) pack.putString("sound", CalamitySounds.TERMINUS_DEACTIVATE.name());
        } else {
            pack.putFloat("power", (1 - countdown / 100f) * 4f);
            if (countdown == 98) pack.putString("sound", CalamitySounds.TERMINUS_ACTIVATE.name());
            else if (countdown == 1 && !inProgress()) pack.putString("sound", CalamitySounds.TERMINUS_CHARGE.name());
        }

        if (!pack.isEmpty()) response.sendToAllClient();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void victoryEvent() {
        super.victoryEvent();
        ServerLevel overWorld = null;
        for (ServerPlayer player : super.getAllPlayer()) {
            if (player.serverLevel().dimension() != Level.OVERWORLD) {
                player.changeDimension(overWorld == null ? (overWorld = player.getServer().getLevel(Level.OVERWORLD)) : overWorld);
                player.connection.teleport(original.x, original.y, original.z, player.getYRot(), player.getXRot());
            }
        }
    }

    @Override
    protected void endEvent() {
        restoreDefaultVenue();
        super.endEvent();
    }
}
