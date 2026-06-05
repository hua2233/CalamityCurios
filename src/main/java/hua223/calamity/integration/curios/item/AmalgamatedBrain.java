package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ShadowsRain;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = AmalgamatedBrain.class, isRoot = true)
public class AmalgamatedBrain extends BaseCurio implements ICuriosStorage {
    public AmalgamatedBrain(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "amalgamated_brain", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new VariableAttributeModifier(uuid, "amalgamated_brain", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (!cooldowns.isOnCooldown(this)) {
                ShadowsRain.of(listener.entity, player, 6);
                cooldowns.addCooldown(this, 100);
            }


            if (player.getRandom().nextDouble() < 0.2)
                additionalDebuff(listener.entity);
        }

        if (CalamityHelp.isCanDodge(player, listener.baseAmount, 2, (int) Mth.clamp(listener.baseAmount * 40, 300, 1800))) {
            listener.canceledEvent();
            AttributeInstance instance = player.getAttribute(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get());
            VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(getFirstUUID(player));
            modifier.setValue(0.1, instance);
            DelayRunnable.addRunTask(160, () -> modifier.setValue(0, instance));
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageCount() {
        return false;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    protected void additionalDebuff(LivingEntity target) {
        target.addEffect(new MobEffectInstance(CalamityEffects.CONFUSED.get(), 80));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "amalgamated_brain", 1, 2, 3, 4, 5, 6);
        return tooltips;
    }
}
