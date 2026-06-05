package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class MarniteRepulsionShield extends BaseCurio {
    public MarniteRepulsionShield(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        DamageSource source = listener.source;
        Vec3 sourcePos = source.getSourcePosition();
        if (sourcePos == null) return;
        if (listener.isTriggerByLiving) {
            Vec3 look = listener.player.getLookAngle();
            Vec3 playerPos = listener.player.position();
            Vec3 relativePos = sourcePos.subtract(playerPos).normalize();

            double d = look.dot(relativePos);
            if (d < 0) {
                listener.entity.hurt(listener.player.damageSources().magic(), 3);
            }
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "shield", 2, AttributeModifier.Operation.ADDITION));
        
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("marnite_repulsion_shield", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("marnite_repulsion_shield", 1).withStyle(ChatFormatting.DARK_AQUA));
        return tooltips;
    }
}
