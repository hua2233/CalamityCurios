package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class BraveryBadge extends BaseCurio {
    public BraveryBadge(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CLOSE_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "critical_strike_chance", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.ARMOR_PENETRATE.get(),
            new AttributeModifier(uuid, "armor_penetrate", 5, AttributeModifier.Operation.ADDITION));

        modifier.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, "attack", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
