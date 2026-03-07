package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Baroclaw extends BaseCurio {
    public Baroclaw(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            CalamityHelp.counterattack(listener.entity, listener.player, listener.baseAmount * 0.35f + 2f,
                100, 0, CalamityEffects.CRUMBLING.get(), CalamityEffects.CRUSH_DEPTH.get());
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "baroclaw", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "baroclaw", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("baroclaw"));
        return tooltips;
    }
}
