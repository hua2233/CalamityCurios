package hua223.calamity.register.rune.stone;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

public class Silence extends RuneStone {
    @Override
    protected String prefix() {
        return "silence";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(), new Tuple<>(.04, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
