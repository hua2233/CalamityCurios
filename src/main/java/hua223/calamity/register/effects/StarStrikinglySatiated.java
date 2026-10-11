package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StarStrikinglySatiated extends CalamityEffect {
    public StarStrikinglySatiated(MobEffectCategory category, int color) {
        super(category, color);
        String id = getSelfUUID();
        addAttributeModifier(Attributes.ARMOR, id, 6, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ATTACK_SPEED, id, 0.15, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, id, 0.125, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, id, 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, id, 0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(), id, 0.06, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 100 == 0;
    }

    @Override
    public void applyServerTick(@NotNull LivingEntity target, int amplifier) {
        if (target.getHealth() < target.getMaxHealth())
            target.heal(2f + target.getMaxHealth() * 0.08f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("star_strikingly_satiated")
            .setStyle(Style.EMPTY.withColor(0xF38BBC)));
    }
}
