package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import static hua223.calamity.register.Items.CalamityItems.BLOODY_WORM_SCARF;

public class BloodyWormTooth extends BaseCurio {
    public BloodyWormTooth(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!BLOODY_WORM_SCARF.isEquip(equipped)) {
            modifier.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(uuid, "attack_bonus", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "armor_bonus", 7, AttributeModifier.Operation.ADDITION));
        
    }
}
