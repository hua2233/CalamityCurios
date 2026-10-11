package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CurioRepel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@CurioRepel(SprintCurio.class)
public class EvasionScarf extends CounterScarf {
    public EvasionScarf(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent(20)
    public final void hurt(HurtListener listener) {
        super.onHurt(listener);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CLOSE_RANGE.get(),
            new AttributeModifier(uuid, "evasion", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
