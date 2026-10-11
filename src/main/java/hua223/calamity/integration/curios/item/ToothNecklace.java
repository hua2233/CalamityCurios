package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ToothNecklace extends BaseCurio {
    private final int penetrate;
    private final float damageUp;

    public ToothNecklace(Properties properties, int penetrate, float damageUp) {
        super(properties);
        this.penetrate = penetrate;
        this.damageUp = damageUp;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.ARMOR_PENETRATE.get(),
            new AttributeModifier(uuid, "tooth_necklace", penetrate, AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "tooth_necklace", damageUp, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
