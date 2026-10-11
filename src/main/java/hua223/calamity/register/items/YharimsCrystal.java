package hua223.calamity.register.items;

import hua223.calamity.render.item.IPrismRender;
import hua223.calamity.render.item.YharimsCrystalController;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
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
        return stack;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, @NotNull ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && player.calamity$IsPlayer) {
            YharimsCrystalController renderer = player.calamity$Player.Calamity$Player.getRenderer();
            renderer.tick(remainingUseDuration);
        }
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
                player.Calamity$Player.startPlayerPostRender(new YharimsCrystalController((AbstractClientPlayer) player));
                onUseTick(level, player, stack, 0);
            }

        }

        //禁用手臂使用摆动，因为那看起来会很奇怪
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
