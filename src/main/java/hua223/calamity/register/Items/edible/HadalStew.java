package hua223.calamity.register.Items.edible;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HadalStew extends Item {
    public HadalStew(Properties properties) {
        super(properties.food(new FoodProperties.Builder().effect(() ->
            new MobEffectInstance(CalamityEffects.PLENTY_SATISFIED.get(), 12000, 0), 1f)
            .saturationMod(5).nutrition(7).meat().build()).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        return super.use(level, player, usedHand);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.heal(6f);
            CalamityCapProvider.safetyRunCalamityMagic(entity, expand ->
                expand.calamity$ChangeMana(100, true));
            if (entity.calamity$IsPlayer) entity.calamity$Player.getCooldowns().addCooldown(this, 600);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag isAdvanced) {
        tooltips.add(CMLangUtil.getTranslatable("hadal_stew", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.getTranslatable("hadal_stew", 2).withStyle(ChatFormatting.YELLOW));
    }
}
