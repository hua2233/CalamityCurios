package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class AstralInjection extends CalamityEffect implements IEffectsCallBack {
    public AstralInjection(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(AttributeRegistry.MANA_REGEN.get(), "50cd184c-6dc0-486d-b52b-bb73cb5cc415", 10, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.level.isClientSide)
            target.hurt(CalamityDamageSource.getAstralInjection(), 4);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (!entity.hasEffect(this) || effect.getDuration() > entity.getEffect(this).getDuration())
            entity.addEffect(new MobEffectInstance(CalamityEffects.MANA_SICKNESS.get(), (int) (effect.getDuration() * 0.8f)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("astral_injection").withStyle(ChatFormatting.AQUA));
    }
}
