package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VoidTouch extends CalamityEffect {
    public VoidTouch(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc417", 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 30 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.level.isClientSide)
            target.hurt(DamageSource.OUT_OF_WORLD, 3 * (amplifier + 1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("void_touch").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
