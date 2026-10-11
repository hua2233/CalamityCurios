package hua223.calamity.register.rune.gem;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class Sacrifical extends OriginGem {
    @Override
    protected String prefix() {
        return "sacrifical";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(-.8, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(-.8, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.DAMAGE_UP.get(), new Tuple<>(.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
