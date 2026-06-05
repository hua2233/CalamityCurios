package hua223.calamity.events;

import hua223.calamity.events.bossentries.EndDragonEntry;
import hua223.calamity.events.bossentries.WardenBossEntry;
import hua223.calamity.events.bossentries.WitherBossEntry;
import hua223.calamity.events.listeners.BaseListener;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.EntityTeleportEvent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

public class BossRushEvent {
    protected static final IDataPackResponse RESPONSE = CalamityItems.TERMINUS.asPackHandler();
    private static boolean bossRushEventActivating = false;
    private static int BOSSER_COUNT;
    protected static List<BossRushBossEntry> BOSSER;
    private static ServerLevel lastVenueLevel;
    private static VoxelShape shape;
    static Vec3 original;

    public static boolean isBossRushEventActivating() {
        return bossRushEventActivating;
    }
    private static void loadBossEntry() {
        BOSSER = new ArrayList<>();
        Collections.addAll(BOSSER, new WardenBossEntry(),
            new WitherBossEntry(), new EndDragonEntry());
        BOSSER.sort(BossRushBossEntry::compareTo);
        BOSSER_COUNT = BOSSER.size();
    }

    private static void spawnNextBoss(ServerPlayer killer, boolean first) {
        if (bossRushEventActivating) {
            final BossRushBossEntry previous = first ? null : BOSSER.remove(BOSSER.size() - 1);
            if (BOSSER.isEmpty()) {
                if (first) failureEvent();
                else if (previous.mustWait())
                    DelayRunnable.addRunTask(previous.getRestTime(), BossRushEvent::victoryEvent);
                else victoryEvent();
            } else {
                BossRushBossEntry bossEntry = BOSSER.get(BOSSER.size() - 1);
                Runnable runnable = () -> {
                    if (bossRushEventActivating) {
                        if (previous != null) bossEntry.bossChange(previous, killer);
                        else BossRushBossEntry.setFirstVenue(bossEntry, killer);
                        bossEntry.spawnBoss(killer);
                    }
                };

                if (first) runnable.run();
                else DelayRunnable.addRunTask(previous.getRestTime(), runnable);
            }
        }
    }

    protected static void setTerminusBoundingBox(ServerLevel level, double x, double z, double size) {
        if (bossRushEventActivating) {
            lastVenueLevel = level;
            WorldBorder border = level.getWorldBorder();
            border.calamity$BossRushBox(x, z, size);
            border.setDamageSafeZone(200);
            shape = Shapes.box(Math.floor(border.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(border.getMinZ()),
                Math.ceil(border.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(border.getMaxZ()));
            RESPONSE.getPack();
            RESPONSE.writeVec3("border", new Vec3(x, z, size));
            RESPONSE.sendToAllClient();
        }
    }

    //针对于特定玩家注册事件
    private static void loadingDynamicEvent() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Set<Map.Entry<EventTypes<?>, MethodHandlerSorter>> events = getEventSet(lookup);
            for (ServerPlayer player : getAllTestingPlayer()) {
                for (Map.Entry<EventTypes<?>, MethodHandlerSorter> entry : events)
                    player.Calamity$Player.addEvent(entry.getKey(), entry.getValue());
            }

            GlobalLoot.ENTITY_LOOTS.mountDynamic( context -> onBossDrop((EntitiesLootContext) context));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static Set<Map.Entry<EventTypes<?>, MethodHandlerSorter>> getEventSet(MethodHandles.Lookup lookup) {
        try {
            HashMap<EventTypes<?>, MethodHandlerSorter> map = new HashMap<>();
            map.put(EventTypes.DEATH, new MethodHandlerSorter(lookup.findStatic(BossRushEvent.class,
                "onPlayerDeath", MethodType.methodType(void.class, DeathListener.class)).asType(
                MethodType.methodType(void.class, BaseListener.class)), BossRushEvent.class, Integer.MAX_VALUE));
            return map.entrySet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static VoxelShape getVenueShape() {
        return shape;
    }

    protected static List<ServerPlayer> getAllTestingPlayer() {
        WorldBorder border = lastVenueLevel.getWorldBorder();
        return lastVenueLevel.getServer().getPlayerList().getPlayers().stream().filter(player ->
            player.isAlive() && player.level() == lastVenueLevel && border.isWithinBounds(player.getBoundingBox())).toList();
    }

    //From Dimension Clone Or Respawn
    public static void onPlayerReSpawn(Player player) {
        if (bossRushEventActivating && player.level() == lastVenueLevel) {
            WorldBorder border = lastVenueLevel.getWorldBorder();
            if (border.isWithinBounds(player.getBoundingBox()))
                player.setPos(border.getMinX() - 5, player.getY(), border.getMinZ() - 5);
        }
    }

    protected static void restoreDefaultVenue() {
        WorldBorder border = lastVenueLevel.getWorldBorder();
        WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
        border.calamity$BossRushBox(settings.getCenterX(), settings.getCenterZ(), settings.getSize());
        RESPONSE.getPack().putString("border", "default");
        RESPONSE.sendToAllClient();
    }

    public static void onPlayerDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            if (listener.player.serverLevel() == lastVenueLevel &&
                lastVenueLevel.getWorldBorder().isWithinBounds(listener.player.getBoundingBox())) {
                listener.player.getCombatTracker().recordDamage(CalamityDamageSource.getCustomizeDeathMessages(Component.translatable(
                    "boss_rush_failure").setStyle(Style.EMPTY.withColor(0xFAD54D)), listener.player.damageSources()), 0f);

                List<ServerPlayer> players = getAllTestingPlayer();
                if (players.isEmpty()) failureEvent();
                else {
                    Mob boss = BOSSER.get(BOSSER.size() - 1).getBoss();
                    if (boss != null && (boss.getTarget() == null || !boss.getTarget().isAlive())) {
                        players.sort((p1, p2) -> (int) (boss.distanceToSqr(p1) - boss.distanceToSqr(p2)));
                        boss.setTarget(players.get(0));
                    }
                }
            }
        }
    }

    public static void onBossDrop(EntitiesLootContext context) {
        if (bossRushEventActivating) {
            BossRushBossEntry entry = BOSSER.get(BOSSER.size() - 1);
            if (context.entity == entry.getBoss()) {
                entry.onKillDrop(context);
                spawnNextBoss(context.player, false);
                if (bossRushEventActivating) {
                    CompoundTag pack = RESPONSE.getPack();
                    pack.putFloat("progress", (float) BOSSER.size() / BOSSER_COUNT);
                    pack.putFloat("interest", entry.getInterestValue());
                    RESPONSE.sendToAllClient();
                }
            }
        }
    }

    public static void siteChanges(int countdown) {
        CompoundTag pack = RESPONSE.getPack();
        if (countdown == 38) pack.putString("sound", bossRushEventActivating ?
            CalamitySounds.TERMINUS_DEACTIVATE.name() : CalamitySounds.TERMINUS_ACTIVATE.name());
        if (!bossRushEventActivating) pack.putFloat("power", 4F - countdown * 0.1F);
        RESPONSE.sendToAllClient();
    }

    public static void startEvent(ServerPlayer player) {
        shape = Shapes.empty();
        original = player.position();
        bossRushEventActivating = true;
        CompoundTag pack = RESPONSE.getPack();
        pack.putFloat("power", 4F);
        pack.putInt("state", 0);
        RESPONSE.sendToAllClient();

        int[] count = {0};
        DelayRunnable.conditionsLoop(() -> {
            if (++count[0] == 1) {
                ServerLevel level = player.serverLevel();
                setTerminusBoundingBox(level, player.position().x, player.position().z, 80);
                loadingDynamicEvent();
            } else if (count[0] == 48) {
                loadBossEntry();
                spawnNextBoss(player, true);
            }
            return !bossRushEventActivating || count[0] == 48;
        }, 20);
    }

    public static void interruptEvent() {
        original = null;
        RESPONSE.getPack().putInt("state", 1);
        RESPONSE.sendToAllClient();
        if (lastVenueLevel != null) endEvent();
    }

    private static void failureEvent() {
        RESPONSE.getPack().putInt("state", 2);
        RESPONSE.sendToAllClient();
        endEvent();
    }

    @SuppressWarnings("ConstantConditions")
    private static void victoryEvent() {
        List<ServerPlayer> players = lastVenueLevel.getServer().getPlayerList().getPlayers();
        Set<Map.Entry<EventTypes<?>, MethodHandlerSorter>> events = getEventSet(MethodHandles.publicLookup());
        ServerLevel level = null;
        loop: for (ServerPlayer player : players) {
            Map<EventTypes<?>, List<MethodHandlerSorter>> library = player.Calamity$Player.getActiveEvents();
            if (library != null) {
                for (Map.Entry<EventTypes<?>, MethodHandlerSorter> entry : events) {
                    List<MethodHandlerSorter> list = library.get(entry.getKey());
                    if (list == null) continue loop;
                    list.remove(entry.getValue());
                }

                if (player.serverLevel().dimension() != Level.OVERWORLD) {
                    player.changeDimension(level == null ? (level = player.getServer().getLevel(Level.OVERWORLD)) : level);
                    player.moveTo(original);
                    player.connection.teleport(original.x, original.y, original.z, player.getYRot(), player.getXRot());
                    RESPONSE.getPack().putInt("state", 3);
                    RESPONSE.sendToClient(player);
                }
            }
        }

        endEvent();
    }

    private static void endEvent() {
        bossRushEventActivating = false;
        restoreDefaultVenue();
        if (BOSSER != null) {
            if (!BOSSER.isEmpty()) BOSSER.get(
                BOSSER.size() - 1).terminationEvent(lastVenueLevel);
            BOSSER = null;
        }
        lastVenueLevel = null;
        original = null;
        GlobalLoot.ENTITY_LOOTS.removeDynamic();
    }

    public static void onTeleport(EntityTeleportEvent event) {
        if (bossRushEventActivating && event.getEntity().level() == lastVenueLevel) {
            WorldBorder border = lastVenueLevel.getWorldBorder();
            if (border.isWithinBounds(event.getPrevX(), event.getPrevZ(), 0) !=
                border.isWithinBounds(event.getTargetX(), event.getTargetZ(), 0))
                event.cancel();
        }
    }
}
