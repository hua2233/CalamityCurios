package hua223.calamity.register.rune.fragments;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

public class Mind extends AncientFragments {
    @Override
    protected String prefix() {
        return "mind";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(AttributeRegistry.MAX_MANA.get(), new Tuple<>(.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
