package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Crumbling extends CalamityEffect {
    protected Crumbling(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ARMOR, "123e4567-e89b-12d3-a456-426614174006", 5, AttributeModifier.Operation.ADDITION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("crumbling").withStyle(ChatFormatting.DARK_GRAY));
    }
}
