package hua223.calamity.register.rune.scroll;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class Massive extends EnchantedScroll {
    @Override
    protected String prefix() {
        return "massive";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.25, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.1, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.MOVEMENT_SPEED, new Tuple<>(.3, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
