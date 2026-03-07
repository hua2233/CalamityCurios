package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ExquisitelyStuffed extends CalamityEffect {
    public ExquisitelyStuffed(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ARMOR, "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 4, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ATTACK_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.8, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.45, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.05, AttributeModifier.Operation.ADDITION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getTranslatable("exquisitely_stuffed"));
    }
}
