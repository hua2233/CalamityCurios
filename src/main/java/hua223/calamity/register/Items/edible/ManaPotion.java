package hua223.calamity.register.Items.edible;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.mixed.ICalamityMagicExpand;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ManaPotion extends Item implements Comparable<ManaPotion> {
    private final int manaValue;
    private final int drinkDuration;
    private boolean addManaSickness = true;

    public ManaPotion(Properties properties, int manaValue, int drinkDuration) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(0).saturationMod(0).alwaysEat().build()));
        this.manaValue = manaValue;
        this.drinkDuration = drinkDuration;
    }

    public ManaPotion(Properties pProperties, int manaValue, int drinkDuration, boolean addManaSickness) {
        super(pProperties);
        this.manaValue = manaValue;
        this.drinkDuration = drinkDuration;
        this.addManaSickness = addManaSickness;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        player.startUsingItem(usedHand);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            CalamityCapProvider.safetyRunCalamityMagic(player, expand -> apply(expand, true, player));
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return drinkDuration;
    }

    @Override
    public int compareTo(@NotNull ManaPotion potion) {
        return potion.manaValue - manaValue;
    }

    public void apply(ICalamityMagicExpand data, boolean sync, ServerPlayer player) {
        data.calamity$ChangeMana(manaValue, sync);

        if (addManaSickness) {
            MobEffect effect = CalamityEffects.MANA_SICKNESS.get();
            if (player.hasEffect(effect)) {
                MobEffectInstance instance = player.getEffect(effect);
                instance.calamity$SetDuration(Math.min(instance.getDuration() + 200, 1200));
            } else player.addEffect(new MobEffectInstance(effect, 200));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag isAdvanced) {
        tooltips.add(CMLangUtil.getDynamic("mana_potion", manaValue));
        if (!addManaSickness) tooltips.add(CMLangUtil.getTranslatable("mana_potion", 1));
    }
}
