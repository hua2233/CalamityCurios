package hua223.calamity.register.rune.gem;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class DestinedGreatness extends OriginGem {
    @Override
    protected String prefix() {
        return "destined_greatness";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(
            Attributes.LUCK, new Tuple<>(1D, AttributeModifier.Operation.ADDITION),
            Attributes.ARMOR, new Tuple<>(.05, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.03, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(), new Tuple<>(.04, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
