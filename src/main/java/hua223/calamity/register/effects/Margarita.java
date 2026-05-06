package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Margarita extends CalamityEffect {
    public Margarita(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ARMOR, "50cd184c-6dc0-486d-b52b-bb73cb5cc415", -5, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "50cd184c-6dc0-486d-b52b-bb73cb5cc415", -2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("margarita").withStyle(ChatFormatting.YELLOW));
    }
}
