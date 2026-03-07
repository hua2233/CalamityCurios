package hua223.calamity.register.config;

import hua223.calamity.register.effects.CalamityEffects;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.UUID;

public class CalamityConfigHelper {
    //Global
    private static final int[] MOB_COUNT_AND_SPAWN_RATE_MODIFY = {0, 0};
    private static final Map<UUID, ServerPlayerConfigData> PLAYER_CONFIG_DATA_MAP = new Object2ObjectOpenHashMap<>();

    static double zenRateAmplifier;
    static double zergRateAmplifier;
    static double zenNumberAmplifier;
    static double zergNumberAmplifier;
    static int zergSpawnCount;

    public static void remove(ServerPlayer player) {
        PLAYER_CONFIG_DATA_MAP.remove(player.getUUID());
        if (player.hasEffect(CalamityEffects.ZEN.get()))
            MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]--;

        if (player.hasEffect(CalamityEffects.ZERG.get()))
            MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]--;
    }

    public static double getSpawnNumberAmplifier() {
        float zen = MOB_COUNT_AND_SPAWN_RATE_MODIFY[0];
        float zerg = MOB_COUNT_AND_SPAWN_RATE_MODIFY[1];
        return zen == zerg ? 1f : zen > zerg ? zenNumberAmplifier : zergNumberAmplifier;
    }

    public static boolean noConflict() {
        return MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] != MOB_COUNT_AND_SPAWN_RATE_MODIFY[1];
    }

    public static boolean getZenRandom(RandomSource source) {
        return source.nextFloat() < zenRateAmplifier;
    }

    public static boolean isZen() {
        return noConflict() && MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] > 0f;
    }

    public static boolean isZerg() {
        return noConflict() && MOB_COUNT_AND_SPAWN_RATE_MODIFY[1] > 0f;
    }

    public static boolean hasMobSpawnInfluence() {
        float zen = MOB_COUNT_AND_SPAWN_RATE_MODIFY[0];
        float zerg = MOB_COUNT_AND_SPAWN_RATE_MODIFY[1];
        return zen != zerg && (zerg > 0 || zen > 0);
    }

    public static boolean getZergRandom(RandomSource source) {
        return source.nextFloat() < zergRateAmplifier;
    }

    public static int getZergSpawnCount() {
        return zergSpawnCount;
    }

    public static void zenState(boolean set) {
        if (set) MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]++;
        else MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]--;
    }

    public static void zergState(boolean set) {
        if (set) MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]++;
        else MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]--;
    }

    public static int getInterval() {
        return MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] > MOB_COUNT_AND_SPAWN_RATE_MODIFY[1] ? 800 : 100;
    }

    private static class ServerPlayerConfigData {
        ServerPlayerConfigData() {}
    }
}
