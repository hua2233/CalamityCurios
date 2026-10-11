package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Purity extends BaseCurio {
    public Purity() {
        super(RegisterList.RARE_ONE);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "purity", .2, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.DAMAGE_UP.get(), new AttributeModifier(uuid, "purity", .3, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ATTACK_SPEED, new AttributeModifier(uuid, "purity", .4, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(MobEffects.DARKNESS, MobEffects.BLINDNESS);
    }

    @ApplyEvent(0)
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        listener.canceledEvent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("purity", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("purity", 2).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
