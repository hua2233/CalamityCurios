package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ElementalQuiver extends DeadshotBrooch {

    public ElementalQuiver(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.FAR_ATTACK.get(),
            new AttributeModifier(uuid, "elemental_quiver", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "elemental_quiver", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.AMMUNITION_ADD.get(),
            new AttributeModifier(uuid, "elemental_quiver", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
