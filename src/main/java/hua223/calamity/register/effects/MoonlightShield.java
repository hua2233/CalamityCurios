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

public class MoonlightShield extends CalamityEffect {
    public MoonlightShield(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
            "50cd184c-6dc0-486d-b52b-bb73cb5cc417", 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.MAGIC_REDUCTION.get(),
            "50cd184c-6dc0-486d-b52b-bb73cb5cc417", 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("moonlight_shield").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
