package hua223.calamity.register.rune.gem;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class GodForged extends OriginGem {
    @Override
    protected String prefix() {
        return "god_forged";
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.08, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.04, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.DAMAGE_UP.get(), new Tuple<>(.03, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.INJURY_OFFSET.get(), new Tuple<>(.04, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
