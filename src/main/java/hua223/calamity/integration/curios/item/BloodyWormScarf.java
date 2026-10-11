package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CurioRepel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@CurioRepel(BloodyWormTooth.class)
public class BloodyWormScarf extends BaseCurio {
    public BloodyWormScarf(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, "bloody_worm_scarf", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "bloody_worm_scarf", 7, AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "bloody_worm_scarf", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
