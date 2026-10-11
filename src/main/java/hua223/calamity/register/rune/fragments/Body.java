package hua223.calamity.register.rune.fragments;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class Body extends AncientFragments {
    @Override
    protected String prefix() {
        return "body";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.MAX_HEALTH, new Tuple<>(.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
