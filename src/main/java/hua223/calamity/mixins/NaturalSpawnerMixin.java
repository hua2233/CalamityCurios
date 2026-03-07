package hua223.calamity.mixins;

import hua223.calamity.register.config.CalamityConfigHelper;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
//    @Unique
//    private static ServerLevel calamity$LevelSnapshot;
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
        boolean has = CalamityConfigHelper.hasMobSpawnInfluence();
        for(MobCategory mobcategory : SPAWNING_CATEGORIES) {
            boolean base = (spawnFriendlies || !mobcategory.isFriendly()) && (spawnMonsters || mobcategory.isFriendly());
            if (has && mobcategory == MobCategory.MONSTER) {
                if (base && (level.getLevelData().getGameTime() % CalamityConfigHelper.getInterval() == 0
                    || !mobcategory.isPersistent()) && spawnState.calamity$CanSpawnForCategory(mobcategory, chunk.getPos(), MAGIC_NUMBER)) {
                    RandomSource source = level.random;
                    if (CalamityConfigHelper.isZen()) {
                        if (!CalamityConfigHelper.getZenRandom(source))
                            spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                    } else {
                        spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                        for (int i = 0; i < CalamityConfigHelper.getZergSpawnCount(); i++)
                            if (CalamityConfigHelper.getZergRandom(source))
                                spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
                    }
                }
            } else if (base && (forcedDespawn || !mobcategory.isPersistent()) && spawnState.canSpawnForCategory(mobcategory, chunk.getPos()))
                spawnCategoryForChunk(mobcategory, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
        }

        level.getProfiler().pop();
    }

//    @Inject(method = "spawnForChunk", at = @At("HEAD"))
//    private static void snapshotCapture(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState, boolean
//        spawnFriendlies, boolean spawnMonsters, boolean forcedDespawn, CallbackInfo ci) {
//        calamity$LevelSnapshot = level;
//    }
//
//    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target =
//        "Lnet/minecraft/world/entity/MobCategory;isPersistent()Z"))
//    private static boolean setInterval(MobCategory instance) {
//        if (instance == MobCategory.MONSTER && CalamityConfigHelper.hasMobSpawnInfluence())
//            //This will be reversed
//            return calamity$LevelSnapshot.getLevelData().getGameTime() % CalamityConfigHelper.getInterval() != 0;
//        return instance.isPersistent();
//    }
//
//    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target =
//        "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategory(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
//    private static boolean zenCancel(NaturalSpawner.SpawnState instance, MobCategory category, ChunkPos pos) {
//        boolean spawn = instance.canSpawnForCategory(category, pos);
//        if (category == MobCategory.MONSTER && CalamityConfigHelper.isZerg())
//            return spawn && CalamityConfigHelper.getZenRandom();
//        return spawn;
//    }
//
//    @Inject(method = "spawnForChunk", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target =
//        "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"))
//    private static void zergSpawn(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState, boolean spawnFriendlies,
//                                  boolean spawnMonsters, boolean forcedDespawn, CallbackInfo ci, MobCategory[] var6, int var, int var1, MobCategory category) {
//        if (category == MobCategory.MONSTER && CalamityConfigHelper.isZerg())
//            for (int i = 0; i < CalamityConfigHelper.getZergSpawnCount(); i++)
//                if (CalamityConfigHelper.getZergRandom())
//                    spawnCategoryForChunk(category, level, chunk, spawnState::canSpawn, spawnState::afterSpawn);
//    }
//
//    @Inject(method = "spawnForChunk", at = @At("TAIL"))
//    private static void clear(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState,
//                              boolean spawnFriendlies, boolean spawnMonsters, boolean forcedDespawn, CallbackInfo ci) {
//        calamity$LevelSnapshot = null;
//    }

    @Mixin(NaturalSpawner.SpawnState.class)
    public static class StateMixin {
        @Shadow @Final private int spawnableChunkCount;

        @Shadow @Final private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

        @Shadow @Final private LocalMobCapCalculator localMobCapCalculator;

        public boolean calamity$CanSpawnForCategory(MobCategory category, ChunkPos pos, double number) {
            if (mobCategoryCounts.getInt(category) < Math.ceil(category.getMaxInstancesPerChunk() * spawnableChunkCount / number
                * CalamityConfigHelper.getSpawnNumberAmplifier())) {
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

        public boolean calamity$CanSpawn(MobCategory instance) {
            return counts.getOrDefault(instance, 0) < Math.ceil(instance.getMaxInstancesPerChunk() * CalamityConfigHelper.getSpawnNumberAmplifier());
        }
    }
}
