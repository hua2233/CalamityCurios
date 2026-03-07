package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ShadowsRain;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = AmalgamatedBrain.class, isRoot = true)
public class AmalgamatedBrain extends BaseCurio {
    public AmalgamatedBrain(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "amalgamated_brain", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving) {
            ShadowsRain.of(listener.entity, player, 6);

            if (player.getRandom().nextDouble() < 0.2)
                additionalDebuff(listener.entity);
        }

        if (CalamityHelp.isCanDodge(player, listener.baseAmount, 2, (int) Mth.clamp(listener.baseAmount * 40, 300, 1800))) {
            listener.event.setCanceled(true);
            AttributeModifier modifier = new AttributeModifier("amalgamated_brain", 0.1, AttributeModifier.Operation.ADDITION);
            AttributeInstance instance = player.getAttribute(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get());
            instance.addTransientModifier(modifier);
            DelayRunnable.addRunTask(160, () -> instance.removeModifier(modifier));
        }
    }

    protected void additionalDebuff(LivingEntity target) {
        target.addEffect(new MobEffectInstance(CalamityEffects.CONFUSED.get(), 80));
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 1));
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 2));
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 3));
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 4));
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 5));
        tooltips.add(CMLangUtil.getTranslatable("amalgamated_brain", 6));
        return tooltips;
    }
}
