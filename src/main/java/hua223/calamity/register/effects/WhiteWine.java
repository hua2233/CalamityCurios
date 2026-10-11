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

public class WhiteWine extends CalamityEffect {
    public WhiteWine(MobEffectCategory category, int color) {
        super(category, color);
        String id = getSelfUUID();
        addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), id, 0.15, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ARMOR, id, -4, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.MAX_HEALTH, id, -0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("white_wine").withStyle(ChatFormatting.YELLOW));
    }
}
