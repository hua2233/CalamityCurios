package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CalamitySigil extends BaseCurio {
    public CalamitySigil(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "sigil", 100, AttributeModifier.Operation.ADDITION));
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new AttributeModifier(uuid, "sigil", 0.15f, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "sigil", 0.1f, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
