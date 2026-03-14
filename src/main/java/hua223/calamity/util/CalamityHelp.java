package hua223.calamity.util;

import hua223.calamity.capability.CalamityCap;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.ApplyKeyEvent;
import hua223.calamity.register.effects.CalamityEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

public class CalamityHelp {
    public static final Vec3 UNIT_X = new Vec3(1, 0, 0);
    public static final Vec3 UNIT_Y = new Vec3(0, 1, 0);
    public static final Vec3 UNIT_Z = new Vec3(0, 0, 1);

    public static final EntityDataSerializer<Short> SHORT = EntityDataSerializer.simple((buf, value) ->
        buf.writeShort(value), FriendlyByteBuf::readShort);
    public static EntityDataAccessor<Short> CALAMITY_DATA_SHARED_FLAGS;
    public static EntityDataAccessor<Boolean> CALAMITY_PROJECTILE_TAG;

    private CalamityHelp() {
    }

    public static boolean hasCurio(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, item).isPresent();
    }

    /**
     * If this accessory implements the ICuriosStorage interface and updates its own item stack in the slot,
     * This method must be used to prevent CuriosApi from accidentally triggering changes and uninstalling NPE caused by ICuriosStorage.
     * @param item Items to be changed
     * @param entity The entity from which the item to be changed comes
     * @param stackConsumer Item stack change callback, which securely updates the item stack through this callback
     */
    public static void safeUpdateItemInSlot(Item item, LivingEntity entity, Consumer<ItemStack> stackConsumer) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve();
        if (optional.isPresent()) {
            for (ICurioStacksHandler stacksHandler : optional.get().getCurios().values()) {
                IDynamicStackHandler dynamicHandler = stacksHandler.getStacks();
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = dynamicHandler.getStackInSlot(i);
                    if (stack.getItem() == item) {
                        stackConsumer.accept(stack);
                        //Ensure that it does not reignite storage calculations, avoid inexplicable anomalies
                        dynamicHandler.setPreviousStackInSlot(i, stack);
                        return;
                    }
                }
            }
        }
    }

    public static void setCalamityFlag(LivingEntity entity, int bit, boolean flag) {
        short b = entity.getEntityData().get(CALAMITY_DATA_SHARED_FLAGS);
        entity.getEntityData().set(CALAMITY_DATA_SHARED_FLAGS, (short) (flag ? b | 1 << bit : b & ~(1 << bit)));
    }

    //0 equip Radiance, 1 sneakingSpeedBonus, 2 field Lock, 3 eternityHex Lock 4 exhausted
    //5 purple Flames Chalice 6 attack 7 fire Immune 8 fluid Stand 9 Sprint CriticalHit
    //10 equip From Deck
    public static boolean getCalamityFlag(LivingEntity entity, int bit) {
        return (entity.getEntityData().get(CALAMITY_DATA_SHARED_FLAGS) & 1 << bit) != 0;
    }

//    public static boolean getCalamityFlags(LivingEntity entity, int... bits) {
//        byte data = entity.getEntityData().get(CALAMITY_DATA_SHARED_FLAGS);
//        for (int i : bits) if ((data & 1 << i) == 0) return false;
//        return true;
//    }

    public static ItemStack getCurio(Player player, Item item) {
        SlotResult result = CuriosApi.getCuriosHelper().findFirstCurio(player, item).orElse(null);
        return result == null ? null : result.stack();
    }

    public static int getDebuffCount(Player player) {
        return (int) player.getActiveEffectsMap().keySet()
            .stream().filter(effect -> !effect.isBeneficial()).count();
    }

    public static boolean hasDebuff(Player player) {
        for (MobEffect effect : player.getActiveEffectsMap().keySet()) {
            if (!effect.isBeneficial()) return true;
        }

        return false;
    }

    public static void counterattack(LivingEntity target, ServerPlayer player, float amount,
                                     int duration, int amplifier, MobEffect... effects) {
        target.hurt(new CalamityDamageSource("player")
            .setNotTriggerEvent().setOwner(player), amount);

        for (MobEffect effect : effects) {
            if (!target.hasEffect(effect))
                target.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    public static boolean isCanDodge(ServerPlayer player, float damage, float threshold, int cd) {
        if (damage > threshold) {
            MobEffect effect = CalamityEffects.DODGE_CD.get();
            boolean can = !player.hasEffect(effect);
            if (can) {
                player.addEffect(new MobEffectInstance(effect, cd));
            }

            return can;
        }

        return false;
    }

    public static boolean silent(Player player) {
        if (CalamityCap.isCalamity(player) && CalamityCap.isInverted(CalamityCap.CurseType.SILVA, player))
            return true;
        else if (player.hasEffect(CalamityEffects.ANECHOIC_COATING.get())) {
            float silence = 0.5f + player.getEffect(CalamityEffects.ANECHOIC_COATING.get()).getAmplifier() * 0.15f;
            return silence > player.getRandom().nextFloat();
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void applyFluidWalk(Player player) {
        if (player.getEyeInFluidType() == ForgeMod.EMPTY_TYPE.get()) {
            BlockPos pos = player.blockPosition();
            Level level = player.level;
            if (level.getFluidState(pos.above()).isEmpty() && canWalkInFluid(level.getFluidState(pos))) {
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, Math.max(0.0, motion.y), motion.z);
                player.setOnGround(true);
                float distance = Math.min(0.1f, (float) motion.horizontalDistance());
                player.bob += (distance - player.bob) * 0.4F;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean canWalkInFluid(FluidState state) {
        return state.is(FluidTags.LAVA) || state.is(FluidTags.WATER);
    }

    public static void setKeyEvent(ServerPlayer player, String name, boolean isApply) {
        NetMessages.sendToClient(new ApplyKeyEvent(name, isApply), player);
    }

    /**
     * 获取视角内最近的敌对活体。不会被不满足条件的Entity阻挡
     *
     * @param player   视线来源的玩家
     * @param level    当前世界
     * @param distance 视线的最远距离
     * @return 最近的敌对活体，可能为null
     */
    public static LivingEntity getLookedEntity(Player player, Level level, int distance) {
        Vec3 lookAngle = player.getLookAngle();
        LivingEntity entity = null;
        double d = 0.2;
        Vec3 vec3;
        double length;

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(lookAngle.scale(distance));
        var list = level.getEntities((Entity) null, new AABB(start, end).inflate(1.0),
            e -> e instanceof Enemy && e.isAlive() && e.isPickable() && !e.isAlliedTo(player));

        for (Entity entity1 : list) {
            vec3 = player.position().vectorTo(entity1.position());
            length = vec3.length();
            if (length < 50.0) {
                vec3 = vec3.scale(1.0 / length);
                length = vec3.distanceTo(lookAngle);
                if (vec3.distanceTo(lookAngle) < d) {
                    d = length;
                    entity = (LivingEntity) entity1;
                }
            }
        }

        return entity;
    }

    /**
     * 通过视线检测实体，只包含第一个实体。如果实体不存在或不满足条件均会返回null
     *
     * @param player   视线来源的玩家
     * @param level    当前世界
     * @param distance 视线的最远距离
     * @return 视线中最近的活体，可能为null
     */
    public static LivingEntity getSightDetectionEntityResult(Player player, Level level, int distance) {
        Vec3 from = player.getEyePosition();
        Vec3 to = player.getLookAngle().normalize().scale(distance).add(from);
        EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, from, to, new AABB(from, to).inflate(1.0),
            entity -> entity.isPickable() && entity.isAlive() &&
                !entity.isAlliedTo(player) && entity instanceof LivingEntity);

        return result == null ? null : (LivingEntity) result.getEntity();
    }

    public static LivingEntity getClosestTarget(Entity entity, int scope, Vec3 cutterPos) {
        return (LivingEntity) entity.level.getEntities(entity, entity.getBoundingBox().inflate(scope))
            .stream().filter(target -> target.isPickable() && target.isAlive() && target.isAttackable()
                && !target.isAlliedTo(entity) && target instanceof Enemy).min(Comparator.comparingDouble(
                    target -> target.distanceToSqr(cutterPos))).orElse(null);
    }

    public static float cosineInterpolation(float start, float end, float time) {
        float cosineTime = (1f - (float) Math.cos(Mth.PI * time)) * 0.5f;
        return start * (1 - cosineTime) + end * cosineTime;
    }

    public static Vec3[] makeBasisFromDirection(Vec3 direction) {
        Vec3 zAxis = direction.normalize();
        Vec3 ref = Math.abs(zAxis.x) > Math.abs(zAxis.z) ? CalamityHelp.UNIT_Z : CalamityHelp.UNIT_X;

        Vec3 xAxis = zAxis.cross(ref).normalize();
        Vec3 yAxis = zAxis.cross(xAxis);
        return new Vec3[]{xAxis, yAxis, zAxis};
    }

    public static AABB rotateAABBAroundZAxis(AABB aabb, Vec3 origin, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double centerX = aabb.getCenter().x - origin.x;
        double centerY = aabb.getCenter().y - origin.y;

        double halfWidth = aabb.getXsize() / 2;
        double halfHeight = aabb.getYsize() / 2;

        double[][] directions = {{halfWidth, halfHeight}, {halfWidth, -halfHeight},
            {-halfWidth, halfHeight}, {-halfWidth, -halfHeight}};

        double firstRotatedX = directions[0][0] * cos - directions[0][1] * sin;
        double firstRotatedY = directions[0][0] * sin + directions[0][1] * cos;

        double maxX = firstRotatedX;
        double minX = firstRotatedX;
        double maxY = firstRotatedY;
        double minY = firstRotatedY;

        for (int i = 1; i < directions.length; i++) {
            double[] dir = directions[i];
            double rotatedX = dir[0] * cos - dir[1] * sin;
            double rotatedY = dir[0] * sin + dir[1] * cos;

            maxX = Math.max(maxX, rotatedX);
            minX = Math.min(minX, rotatedX);
            maxY = Math.max(maxY, rotatedY);
            minY = Math.min(minY, rotatedY);
        }

        double rotatedCenterX = centerX * cos - centerY * sin;
        double rotatedCenterY = centerX * sin + centerY * cos;

        double newMinX = rotatedCenterX + minX + origin.x;
        double newMaxX = rotatedCenterX + maxX + origin.x;
        double newMinY = rotatedCenterY + minY + origin.y;
        double newMaxY = rotatedCenterY + maxY + origin.y;

        return new AABB(newMinX, newMinY, aabb.minZ, newMaxX, newMaxY, aabb.maxZ);
    }

    public static Vec3 calculateReflection(RandomSource source, Vec3 incoming, Vec3 normal) {
        Vec3 incomingNor = incoming.normalize();
        double dotProduct = incomingNor.dot(normal);
        double angle = Math.acos(Math.abs(dotProduct));

        if (angle < Math.PI / 4) {
            Vec3 newDir = incomingNor.subtract(normal.scale(2 * dotProduct));
            return newDir.add(
                (source.nextDouble() - 0.5) * 0.2,
                (source.nextDouble() - 0.5) * 0.2,
                (source.nextDouble() - 0.5) * 0.2
            ).normalize();
        } else {
            double normalComponent = incoming.dot(normal);
            Vec3 normalVector = normal.scale(-0.6 * normalComponent);
            Vec3 tangentVector = incoming.subtract(normal.scale(normalComponent)).scale(0.9);
            return normalVector.add(tangentVector).normalize();
        }
    }
}
