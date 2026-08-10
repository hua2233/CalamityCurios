package hua223.calamity.mixins;

import hua223.calamity.register.effects.Zen;
import hua223.calamity.register.effects.Zerg;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.*;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Shadow
    public static void spawnCategoryForChunk(MobCategory category, ServerLevel level, LevelChunk chunk,
                                             NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback) {}

    @Shadow @Final
    private static MobCategory[] SPAWNING_CATEGORIES;

    @Shadow @Final
    static int MAGIC_NUMBER;

    /**
     * @author hua223
     * @reason 1.19.2 vanilla logic + Calamity amplification. Insert a piece of logic for better control
     */
    @Overwrite
    public static void spawnForChunk(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState,
                                      boolean spawnFriendlies, boolean spawnMonsters, boolean forcedDespawn) {
        level.getProfiler().push("spawner");
        boolean has = Zen.hasMobSpawnInfluence();
        for(MobCategory mobcategory : SPAWNING_CATEGORIES) {
            boolean base = (spawnFriendlies || !mobcategory.isFriendly()) && (spawnMonsters || mobcategory.isFriendly());
            if (has && mobcategory == MobCategory.MONSTER) {
                if (base && (level.getLevelData().getGameTime() % Zen.getInterval() == 0
                    || !mobcategory.isPersistent()) && spawnState.calamity$CanSpawnForCategory(mobcategory, chunk.getPos(), MAGIC_NUMBER)) {
                    RandomSource source = level.random;
                    if (Zen.isZen()) {
                        if (source.nextFloat() >= Zen.ZEN_RATE_AMPLIFIER)
                            spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                    } else {
                        spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                        for (int i = 0; i < Zerg.ZERG_SPAWN_COUNT; i++)
                            if (source.nextFloat() < Zerg.ZERG_RATE_AMPLIFIER)
                                spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                    }
                }
            } else if (base && (forcedDespawn || !mobcategory.isPersistent()) && spawnState.canSpawnForCategory(mobcategory, chunk.getPos()))
                spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
        }

        level.getProfiler().pop();
    }

    @Mixin(NaturalSpawner.SpawnState.class)
    public static class StateMixin {
        @Shadow @Final private int spawnableChunkCount;

        @Shadow @Final private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

        @Shadow @Final private LocalMobCapCalculator localMobCapCalculator;

        @Unique
        public boolean calamity$CanSpawnForCategory(MobCategory category, ChunkPos pos, double number) {
            if (mobCategoryCounts.getInt(category) < Math.ceil(category.getMaxInstancesPerChunk() * spawnableChunkCount / number
                * Zen.getSpawnNumberAmplifier())) {
                for(ServerPlayer serverplayer : localMobCapCalculator.getPlayersNear(pos)) {
                    LocalMobCapCalculator.MobCounts mobcounts = localMobCapCalculator.playerMobCounts.get(serverplayer);
                    if (mobcounts == null || mobcounts.calamity$CanSpawn(category))
                        return true;
                }
            }

            return false;
        }
    }

    @Mixin(LocalMobCapCalculator.MobCounts.class)
    public static class LocalMobCapMixin {
        @Shadow @Final private Object2IntMap<MobCategory> counts;

        @Unique
        public boolean calamity$CanSpawn(MobCategory instance) {
            return counts.getOrDefault(instance, 0) < Math.ceil(instance.getMaxInstancesPerChunk() * Zen.getSpawnNumberAmplifier());
        }
    }
}
