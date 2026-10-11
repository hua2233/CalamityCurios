package hua223.calamity.register.rune.scroll;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class Reckless extends EnchantedScroll {
    @Override
    protected String prefix() {
        return "reckless";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(-.5, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.DAMAGE_UP.get(), new Tuple<>(.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
