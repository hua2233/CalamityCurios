package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
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

import java.util.List;
import java.util.UUID;

public class EldritchSoulArtifact extends BaseCurio {
    public EldritchSoulArtifact(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onFiredProjectile(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += .25;
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        LivingEntity target = listener.entity;
        MobEffect effect = CalamityEffects.WHISPERING_DEATH.get();
        if (target.hasEffect(effect)) {
            int level = target.getEffect(effect).getAmplifier();
            if (level >= 2) return;
            target.addEffect(new MobEffectInstance(effect, 160, level + 1));
        } else {
            target.addEffect(new MobEffectInstance(effect, 160));
        }
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.WHISPERING_DEATH.get());
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(uuid, "eldritch", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "eldritch", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("eldritch", 1));
        tooltips.add(CMLangUtil.getTranslatable("eldritch", 2));
        return tooltips;
    }
}
