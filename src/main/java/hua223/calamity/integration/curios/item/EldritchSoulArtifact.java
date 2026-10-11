package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.events.listeners.ProjectileSpawnListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
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
    public EldritchSoulArtifact(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onFiredProjectile(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += .25;
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        LivingEntity target = listener.entity;
        MobEffect effect = CalamityEffects.WHISPERING_DEATH.get();
        MobEffectInstance instance = target.getEffect(effect);
        if (instance == null)  listener.entity.addEffect(new MobEffectInstance(effect, 160, 0));
        else if (instance.getAmplifier() < 2) instance.calamity$SetProperties(160, instance.getAmplifier() + 1, listener.entity);
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
            new AttributeModifier(uuid, "eldritch", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "eldritch", 1, 2);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("eldritch", 3).withStyle(ChatFormatting.DARK_RED));
        return tooltips;
    }
}
