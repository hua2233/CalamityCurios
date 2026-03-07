package hua223.calamity.register.Items;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class YharimsCrystal extends Item {
    public YharimsCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && level.isClientSide)
            YharimsCrystalRenderer.stop((LocalPlayer) entity);
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player.level.isClientSide) {
            YharimsCrystalRenderer.update(player);
            if (count % 10 == 0) {
                player.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    CalamitySounds.PRISM.get(), SoundSource.AMBIENT, 1f, 1f, false);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity.calamity$IsPlayer && level.isClientSide)
            YharimsCrystalRenderer.stop((LocalPlayer) entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (usedHand == InteractionHand.MAIN_HAND && !player.isUsingItem()) {
            player.startUsingItem(usedHand);
            if (level.isClientSide) YharimsCrystalRenderer.start((LocalPlayer) player);
        }
        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1200;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("yharims_crystal").withStyle(ChatFormatting.GOLD));
    }
}
