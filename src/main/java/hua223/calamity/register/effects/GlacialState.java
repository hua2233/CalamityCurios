package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GlacialState extends CalamityEffect {
    protected GlacialState(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "123e4567-e89b-12d3-a456-426614174029", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(AttributeRegistry.ICE_MAGIC_RESIST.get(),
            "123e4567-e89b-12d3-a456-426614174029", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("glacial_state").withStyle(ChatFormatting.BLUE));
    }
}
