package hua223.calamity.register.Items;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static hua223.calamity.render.Item.YharimsCrystalRenderer.*;

public class YharimsCrystal extends Item {
    public YharimsCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide) YharimsCrystalRenderer.stop((LocalPlayer) entity);
        else {
            CompoundTag tag = entity.getPersistentData();
            tag.remove("angle");
            tag.remove("rate");
        }
        return stack;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, @NotNull ItemStack stack, int remainingUseDuration) {
        boolean client = level.isClientSide;
        boolean isTriggerTime = remainingUseDuration % 10 == 0;
        if (!client && !isTriggerTime) return;

        float chargeRatio = Mth.clamp((1200 - remainingUseDuration) / 60f, 0f, 1f);
        if (client) System.arraycopy(endPos, 0, lastEndPos, 0, endPos.length);

        float radius;
        float spinRate;
        float circleStartAngle;
        if (client) {
            spinRate = YharimsCrystalRenderer.spinRate;
            circleStartAngle = YharimsCrystalRenderer.circleStartAngle;
        } else {
            CompoundTag tag = player.getPersistentData();
            circleStartAngle = tag.getFloat("angle");
            spinRate = tag.getFloat("rate");
        }

        if (chargeRatio < 1) {
            radius = Mth.lerp(chargeRatio, 16, 0.05f);
            if (client) YharimsCrystalRenderer.scale = Mth.lerp(chargeRatio, 0.05f, 0.2f);

            if (chargeRatio <= 0.66f) {
                float phaseRatio = chargeRatio * 1.5f;
                spinRate = Mth.lerp(phaseRatio, 0, 16f);
            } else {
                float phaseRatio = (chargeRatio - 0.66f) * 3f;
                spinRate = Mth.lerp(phaseRatio, 8, 40f);
            }
        } else radius = player.getRandom().nextFloat() * 0.05f;

        if ((circleStartAngle += (float) Math.toRadians(spinRate)) > Mth.TWO_PI)
            circleStartAngle -= Mth.TWO_PI;

        if (client) {
            lastRotateAngle = rotateAngle;
            if ((rotateAngle += spinRate) > 180) rotateAngle -= 360;
            YharimsCrystalRenderer.circleStartAngle = circleStartAngle;
            YharimsCrystalRenderer.spinRate = spinRate;
        } else {
            CompoundTag tag = player.getPersistentData();
            tag.putFloat("angle", circleStartAngle);
            tag.putFloat("rate", spinRate);
        }

        Vec3[] dir = CalamityHelp.makeBasisFromDirection(YharimsCrystal.yRotDir(player));
        Vec3 startPos = player.position().add(0, player.getEyeHeight() * 0.7, 0).add(dir[2]);

        ServerLevel serverLevel = client ? null : (ServerLevel) level;
        DamageSource source = null;
        float baseAttack = 0;
        for (int i = 0; i < endPos.length; i++) {
            double angle = circleStartAngle + (2 * Math.PI * i / 6);
            Vec3 target = startPos.add(dir[2].scale(20f)).add(dir[0].scale(
                radius * Math.cos(angle)).add(dir[1].scale(radius * Math.sin(angle))));

            if (client) {
                BlockHitResult hit = player.level().clip(new ClipContext(startPos, target,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                endPos[i] = (hit.getType() != HitResult.Type.MISS ? hit.getLocation() : target).subtract(startPos);

                if (isTriggerTime) level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    CalamitySounds.PRISM.get(), SoundSource.AMBIENT, 1f, 1f, false);
            } else {
                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(startPos, target))) {
                    if (entity.calamity$IsPlayer || !entity.isAlive() || entity.isAlliedTo(player)) continue;

                    if (source == null) {
                        source = CalamityDamageSource.source(CalamityDamageTypes.PRISM, entity);
                        baseAttack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    }

                    entity.hurt(source, baseAttack);
                }
            }
        }
    }

    public static Vec3 yRotDir(LivingEntity player) {
        float f = player.getXRot() * Mth.DEG_TO_RAD;
        float f1 = (-player.yBodyRot + 4) * Mth.DEG_TO_RAD;
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, LivingEntity entity, int timeCharged) {
        if (entity.calamity$IsPlayer && level.isClientSide)
           YharimsCrystalRenderer.stop((LocalPlayer) entity);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand == InteractionHand.MAIN_HAND && !player.isUsingItem()) {
            player.startUsingItem(usedHand);
            if (level.isClientSide) {
                YharimsCrystalRenderer.start(null);
                onUseTick(level, player, stack, 0);
                YharimsCrystalRenderer.start((LocalPlayer) player);
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 1200;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final HumanoidModel.ArmPose armPose = HumanoidModel.ArmPose.create("YHARIMS_CRYSTAL", true,
                (model, entity, arm) -> {
                    float entityRot = (entity.getXRot() + 8) * Mth.DEG_TO_RAD;
                    float armXRot = entityRot - 1.5708f;
                    //armXRot += (float) Math.toRadians(-45);
                    model.rightArm.xRot = armXRot;
                    model.leftArm.xRot = armXRot;

                    float armYRot = 5 * Mth.DEG_TO_RAD;//(float) Math.toRadians(5);
                    model.rightArm.yRot = -armYRot ;
                    model.leftArm.yRot = armYRot;

                    float armZRot = 10 * Mth.DEG_TO_RAD;//(float) Math.toRadians(10);
                    model.rightArm.zRot = -armZRot ;
                    model.leftArm.zRot = armZRot;
                });
            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return entityLiving.calamity$IsPlayer && entityLiving.calamity$Player.isUsingItem() ? armPose : null;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("yharims_crystal").withStyle(ChatFormatting.GOLD));
    }
}
