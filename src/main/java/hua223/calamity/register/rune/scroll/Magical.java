package hua223.calamity.register.rune.scroll;

import hua223.calamity.register.attribute.CalamityAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

public class Magical extends EnchantedScroll {
    @Override
    protected String prefix() {
        return "magical";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(AttributeRegistry.MAX_MANA.get(), new Tuple<>(30D, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.SPELL_POWER.get(), new Tuple<>(.06, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.MAGIC_REDUCTION.get(), new Tuple<>(.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
