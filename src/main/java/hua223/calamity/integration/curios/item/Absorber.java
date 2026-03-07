package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Absorber extends BaseCurio {
    public Absorber(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "absorber", 10, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "absorber", 0.12, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "absorber", 99.99, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            ServerPlayer player = listener.player;

            if (player.getHealth() < player.getMaxHealth() / 2) {
                ItemCooldowns cooldowns = player.getCooldowns();
                if (!cooldowns.isOnCooldown(this)) {
                    Collection<MobEffectInstance> effects = player.getActiveEffects();

                    if (!effects.isEmpty()) {
                        List<MobEffect> harmful = effects.stream()
                            .map(MobEffectInstance::getEffect).filter(effect -> !effect.isBeneficial()).toList();

                        for (MobEffect effect : harmful) {
                            player.removeEffect(effect);
                        }
                    }

                    List<Player> players = player.level.getNearbyPlayers(TargetingConditions.forNonCombat(), player,
                        player.getBoundingBox().inflate(5));

                    players.add(player);

                    for (Player player1 : players) {
                        player1.addEffect(new MobEffectInstance(CalamityEffects.RE_GENA.get(), 200, 1));
                    }

                    cooldowns.addCooldown(this, 600);
                }
            }

            CalamityHelp.counterattack(listener.entity, listener.player, listener.baseAmount, 200, 0, CalamityEffects.NATURE_PAIN.get());

            player.heal(listener.baseAmount * 0.05f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("absorber", 1));
        tooltips.add(CMLangUtil.getTranslatable("absorber", 2));
        tooltips.add(CMLangUtil.getTranslatable("absorber", 3));
        tooltips.add(CMLangUtil.getTranslatable("absorber", 4));
        return tooltips;
    }
}
