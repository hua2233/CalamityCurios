package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class DeadshotBrooch extends BaseCurio {
    public DeadshotBrooch(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void ArrowSet(ProjectileSpawnListener listener) {
        if (listener.isArrow) {
            listener.speedVectorAmplifier += .3;
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.FAR_ATTACK.get(),
            new AttributeModifier(uuid, "brooch", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "brooch", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.AMMUNITION_ADD.get(),
            new AttributeModifier(uuid, "brooch", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("deadshot_brooch"));
        return tooltips;
    }
}
