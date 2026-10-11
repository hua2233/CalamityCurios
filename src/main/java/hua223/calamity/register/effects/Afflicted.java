package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Afflicted extends CalamityEffect {
    public Afflicted() {
        super(MobEffectCategory.BENEFICIAL, 11141120);
        String uuid = getSelfUUID();
        addAttributeModifier(Attributes.MAX_HEALTH, uuid, .1f, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.INJURY_OFFSET.get(), uuid, .07f, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(CalamityAttributes.DAMAGE_UP.get(), uuid, .1f, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ARMOR, uuid, 13, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, uuid, 6, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(AttributeRegistry.BLOOD_SPELL_POWER.get(), uuid, .1f, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(getDescriptionId()).withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity livingEntity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(livingEntity, attributeMap, 0);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 60 == 0;
    }

    @Override
    public void applyServerTick(@NotNull LivingEntity target, int amplifier) {
        target.heal(target.getMaxHealth() * 0.05f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("afflicted").withStyle(ChatFormatting.DARK_RED));
    }
}
