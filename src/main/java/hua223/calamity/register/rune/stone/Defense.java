package hua223.calamity.register.rune.stone;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class Defense extends RuneStone {
    private final double armor;
    private final String prefix;

    public Defense(String prefix, double armor) {
        this.prefix = prefix;
        this.armor = armor;
    }

    @Override
    protected String prefix() {
        return prefix;
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(armor, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
