package hua223.calamity.register.items;

import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.Item.IPrismRender;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CalamityPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class YharimsCrystal extends Item {
    public YharimsCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (entity.level().isClientSide)
            entity.calamity$Player.Calamity$Player.stopPlayerPostRender();
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
        CalamityPlayer calamityPlayer = player.calamity$Player.Calamity$Player;

        if (!client && !isTriggerTime) return;

        float chargeRatio = Mth.clamp((1200 - remainingUseDuration) / 60f, 0f, 1f);
        if (client) {
            YharimsCrystalRenderer renderer = calamityPlayer.getRenderer();
            System.arraycopy(renderer.endPos, 0, renderer.lastEndPos, 0, renderer.endPos.length);
        }

        float radius;
        float spinRate;
        float circleStartAngle;
        if (client) {
            YharimsCrystalRenderer renderer = calamityPlayer.getRenderer();
            spinRate = renderer.spinRate;
            circleStartAngle = renderer.circleStartAngle;
        } else {
            CompoundTag tag = player.getPersistentData();
            circleStartAngle = tag.getFloat("angle");
            spinRate = tag.getFloat("rate");
        }

        if (chargeRatio < 1) {
            radius = Mth.lerp(chargeRatio, 16, 0.05f);
            if (client) {
                YharimsCrystalRenderer renderer = calamityPlayer.getRenderer();
                renderer.scale = Mth.lerp(chargeRatio, 0.05f, 0.2f);
            }

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
            YharimsCrystalRenderer renderer = calamityPlayer.getRenderer();
            renderer.lastRotateAngle = renderer.rotateAngle;
            if ((renderer.rotateAngle += spinRate) > 180) renderer.rotateAngle -= 360;
            renderer.circleStartAngle = circleStartAngle;
            renderer.spinRate = spinRate;
        } else {
            CompoundTag tag = player.getPersistentData();
            tag.putFloat("angle", circleStartAngle);
            tag.putFloat("rate", spinRate);
        }

        Vec3[] dir = CalamityHelp.makeBasisFromDirection(yRotDir(player));
        Vec3 startPos = player.position().add(0, player.getEyeHeight() * 0.7, 0);

        DamageSource source = null;
        float baseAttack = 0;
        for (int i = 0; i < 6; i++) {
            double angle = circleStartAngle + 2 * Math.PI * (i / 6F);
            Vec3 target = startPos.add(dir[2].scale(20f)).add(dir[0].scale(
                radius * Math.cos(angle)).add(dir[1].scale(radius * Math.sin(angle))));
            //rayResultEndPos
            target = player.level().clip(new ClipContext(startPos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
            Vec3 ray = target.subtract(startPos);

            if (client) {
                YharimsCrystalRenderer renderer = calamityPlayer.getRenderer();
                renderer.endPos[i] = ray;
                if (isTriggerTime) CalamitySounds.PRISM.playLocalSound();
            } else {
                //FIXME Unresolved gaze detection issues
                for (Entity entity : level.getEntities(player, player.getBoundingBox().expandTowards(ray)
                    , entity -> entity.isPickable() && entity.isAlive() && entity instanceof LivingEntity && !entity.isAlliedTo(player))) {
                    if (entity.getBoundingBox().clip(startPos, target).isPresent()) {
                        if (source == null) {
                            source = DamageSupplier.MAGIC_PROJECTILE.get(player);
                            baseAttack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        }

                        entity.hurt(source, baseAttack);
                    }
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
        if (entity.level().isClientSide)
           entity.calamity$Player.Calamity$Player.stopPlayerPostRender();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand == InteractionHand.MAIN_HAND && !player.isUsingItem()) {
            player.startUsingItem(usedHand);
            if (level.isClientSide) {
                player.Calamity$Player.startPlayerPostRender(new YharimsCrystalRenderer((AbstractClientPlayer) player));
                onUseTick(level, player, stack, 0);
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
        return UseAnim.CUSTOM;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(IPrismRender.PRISM_EXTENSIONS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("yharims_crystal").withStyle(ChatFormatting.GOLD));
    }
}
