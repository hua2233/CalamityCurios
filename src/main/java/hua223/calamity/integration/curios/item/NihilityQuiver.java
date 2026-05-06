package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class NihilityQuiver extends BaseCurio {
    public NihilityQuiver(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void ArrowSet(ProjectileSpawnListener listener) {
        if (listener.isArrow) {
            listener.hurtAmplifier += 0.75;
            listener.speedVectorAmplifier += 1;
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "quiver", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
