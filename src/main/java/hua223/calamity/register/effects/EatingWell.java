package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class EatingWell extends CalamityEffect {
    public EatingWell(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ARMOR, "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 1.5, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ATTACK_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 0.025, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 0.025, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 0.08, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 0.15, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            "50cd184c-6dc0-486d-b52b-bb73cb5cc412", 0.03, AttributeModifier.Operation.ADDITION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("well_fed").withStyle(ChatFormatting.YELLOW));
    }
}
