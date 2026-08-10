package hua223.calamity.register.items;

import hua223.calamity.register.entity.RancorMagicCircle;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Rancor extends Item {
    public Rancor(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && !player.isUsingItem()) {
            RancorMagicCircle.create(player);
            player.startUsingItem(hand);
        }

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 10000;
    }

    @Override
    public void onUseTick(@NotNull Level level, LivingEntity player, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!player.level().isClientSide && !player.isAlive()) {
            player.stopUsingItem();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> components, TooltipFlag isAdvanced) {
        components.add(CMLangUtil.getTranslatable("rancor", 1).withStyle(ChatFormatting.DARK_RED));
        components.add(CMLangUtil.getTranslatable("rancor", 2).withStyle(ChatFormatting.DARK_RED));
    }
}
