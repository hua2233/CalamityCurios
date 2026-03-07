package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ManaRegeneration extends CalamityEffect {
    public ManaRegeneration(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(AttributeRegistry.MANA_REGEN.get(),
            "50cd184c-6dc0-486d-b52b-bb73cb5cc413", 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("mana_regeneration").withStyle(ChatFormatting.DARK_AQUA));
    }
}
