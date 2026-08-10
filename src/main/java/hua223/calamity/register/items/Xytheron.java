package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//From StarlessNight Up
public class Xytheron extends SwordItem {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Xytheron() {
        super(RegisterList.DRAGON, 114, -2, RegisterList.ITEM_CALAMITY);
        defaultModifiers = (ImmutableMultimap) ImmutableMultimap.builder().putAll(
            defaultModifiers).put(CalamityAttributes.ARMOR_PENETRATE.get(),
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Xytheron",
                99, AttributeModifier.Operation.ADDITION))
            .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Xytheron", 2, AttributeModifier.Operation.ADDITION)).build();

    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.enchant(RegisterList.SHARED_PAIN.get(), 5);
        return stack;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        MobEffect effect = CalamityEffects.LIFE_OPPRESS.get();
        MobEffectInstance instance = target.getEffect(effect);
        if (instance == null) target.calamity$ForciblyAddEffect(new MobEffectInstance(effect, 200), attacker);
        else if (instance.getAmplifier() < 2)
            instance.calamity$SetProperties(200, instance.getAmplifier() + 1, target);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("xytheron").withStyle(ChatFormatting.AQUA));
        tooltips.add(CMLangUtil.blankLine());
    }
}
