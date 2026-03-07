package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.item.ToothNecklace;
import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class WyrmToothNecklace extends ToothNecklace {
    public WyrmToothNecklace(Properties properties) {
        super(properties, 100, 0.25f);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        super.setAttributeModifiers(uuid, stack, modifier, equipped);
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "tooth_necklace", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
