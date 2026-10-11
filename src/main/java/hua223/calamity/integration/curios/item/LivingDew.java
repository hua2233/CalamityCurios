package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.util.CurioRepel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@CurioRepel(AmbrosialAmpoule.class)
public class LivingDew extends HoneyDew {
    public LivingDew(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void getEffect(EffectListener listener) {
        super.onGetEffect(listener);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "honey_dew", 8, AttributeModifier.Operation.ADDITION));
    }
}
