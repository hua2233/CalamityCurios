package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Deceive extends CalamityEffect {
    public Deceive(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc416", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("deceive").setStyle(Style.EMPTY.withColor(getColor())));
    }
}
