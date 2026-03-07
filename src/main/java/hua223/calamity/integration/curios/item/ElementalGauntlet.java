package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class ElementalGauntlet extends BaseCurio {

    public ElementalGauntlet(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        LivingEntity target = listener.entity;
        MobEffect effect = CalamityEffects.ELEMENTAL_MIX.get();
        if (target.hasEffect(effect)) return;
        target.addEffect(new MobEffectInstance(effect, 600, 0), listener.player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(uuid, "gauntlet", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.CLOSE_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "gauntlet", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, "gauntlet", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ATTACK_KNOCKBACK,
            new AttributeModifier(uuid, "gauntlet", 0.1, AttributeModifier.Operation.ADDITION));

        modifier.put(ForgeMod.ATTACK_RANGE.get(),
            new AttributeModifier(uuid, "gauntlet", 2, AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.CLOSE_RANGE.get(),
            new AttributeModifier(uuid, "gauntlet", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("elemental_gauntlet", 2));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("elemental_gauntlet", 1));
        }
        return tooltips;
    }
}
