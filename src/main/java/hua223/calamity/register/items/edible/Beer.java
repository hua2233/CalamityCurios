package hua223.calamity.register.items.edible;

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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Beer extends Item {
    public Beer(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide && entity.calamity$IsPlayer && getCooldown() > 0)
            entity.calamity$Player.getCooldowns().addCooldown(this, getCooldown());
        endOfUse(stack, level, entity);
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 48;
    }

    protected void endOfUse(ItemStack stack, Level level, LivingEntity entity) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    protected int getCooldown() {
        return 600;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag isAdvanced) {
        tooltips.add(CMLangUtil.getTranslatable("beer_harmful").withStyle(ChatFormatting.YELLOW));
    }
}
