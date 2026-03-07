package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ManaSickness extends CalamityEffect {
    protected ManaSickness(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "50cd184c-6dc0-486d-b52b-bb73cb5cc411",
            0.9f, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("mana_sickness").withStyle(ChatFormatting.RED));
    }
}
