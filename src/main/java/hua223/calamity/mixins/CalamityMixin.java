package hua223.calamity.mixins;

@Deprecated
public class CalamityMixin {
//
//    @Mixin({PiglinAi.class})
//    public abstract static class piglin {
//        @Redirect(method = "findNearestValidAttackTarget", at = @At(value = "INVOKE",
//                target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;isEntityAttackable(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
//        private static boolean redirectCheck(LivingEntity piglin, LivingEntity player) {
//            if (Sensor.isEntityAttackable(piglin, player)) {
//                if (player instanceof ServerPlayer serverPlayer) {
//                    return !Calamity.isCalamity(serverPlayer);
//                }
//            }
//
//            return true;
//        }
//    }
//
//    @Mixin(targets = "net.minecraft.world.entity.monster.Guardian$GuardianAttackSelector")
//    public abstract static class guardian {
//        @Shadow @Final
//        private Guardian guardian;
//
//        @Inject(method = "test(Lnet/minecraft/world/entity/LivingEntity;)Z",
//            at = @At("HEAD"),cancellable = true)
//        private void redirectCheck(LivingEntity pEntity, CallbackInfoReturnable<Boolean> cir) {
//            if (pEntity instanceof ServerPlayer player) {
//                if (guardian.getLastHurtByMob() == player) return;
//                if (Calamity.isCalamity(player)) cir.setReturnValue(false);
//            }
//        }
//    }
//
//    @Mixin({PiglinBruteAi.class})
//    public abstract static class pigligBrute {
//        @Invoker("getTargetIfWithinRange")
//        private static Optional<? extends LivingEntity> invokeGet(AbstractPiglin piglin, MemoryModuleType<LivingEntity> moduleType) {
//            throw new RuntimeException("getTargetIfWithinRange");
//        }
//
//        @Redirect(method = "findNearestValidAttackTarget", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/world/entity/monster/piglin/PiglinBruteAi;getTargetIfWithinRange(Lnet/minecraft/world/entity/monster/piglin/AbstractPiglin;Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;)Ljava/util/Optional;"))
//        private static Optional<? extends LivingEntity> redirectCheck(AbstractPiglin piglin, MemoryModuleType<LivingEntity> moduleType) {
//            Optional<? extends LivingEntity> optional = invokeGet(piglin, moduleType);
//            if (optional.isPresent() && optional.get() instanceof ServerPlayer player) {
//                if (player == piglin.getLastHurtByMob()) return optional;
//                if (Calamity.isCalamity(player)) {
//                    return Optional.empty();
//                }
//            }
//            return optional;
//        }
//    }

//    @Mixin({NearestAttackableTargetGoal.class})
//    public abstract static class target {
//        @Redirect(method = "findTarget", at = @At(value = "INVOKE", target =
//            "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDD)Lnet/minecraft/world/entity/player/Player;"))
//        private Player redirectFind(Level instance, TargetingConditions targetingConditions, LivingEntity entity, double x, double y, double z) {
//            Player player = instance.getNearestPlayer(targetingConditions, entity, x, y, z);
//            if (player != null) {
//                if (player instanceof ServerPlayer serverPlayer && Calamity.isCalamity(serverPlayer)) {
//                    return null;
//                }
//            }
//            return player;
//        }
//    }
//
//    @Mixin({Mob.class})
//    public abstract static class Mob {
//
//    }
}
