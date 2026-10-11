package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Mushy extends CalamityEffect {
    protected Mushy(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        String uuid = getSelfUUID();
        addAttributeModifier(Attributes.ARMOR, uuid, 6, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(CalamityAttributes.DAMAGE_UP.get(), uuid, 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 40 == 0;
    }

    @Override
    public void applyServerTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.heal(2f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("mushy").withStyle(ChatFormatting.BLUE));
    }
}
