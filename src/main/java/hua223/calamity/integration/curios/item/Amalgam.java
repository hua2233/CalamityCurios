package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ConflictChain(AmalgamatedBrain.class)
public class Amalgam extends AmalgamatedBrain {
    public Amalgam(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new VariableAttributeModifier(uuid, "amalgamated_brain", 0, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "amalgam", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "amalgam", 8, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "amalgam", 4, AttributeModifier.Operation.ADDITION));
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (listener.effect.isBeneficial())
            listener.setEffectDuration(listener.instance.getDuration() * 2);
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            Collection<MobEffectInstance> actives = listener.player.getActiveEffects();
            if (!actives.isEmpty()) {
                List<MobEffectInstance> effects =
                    actives.stream().filter(effect -> effect.getEffect().isBeneficial()).toList();
                if (!effects.isEmpty()) {
                    UUID id = listener.player.getUUID();
                    PlayerList players = listener.player.server.getPlayerList();
                    DelayRunnable.conditionsLoop(() -> {
                        if (listener.isCanceled()) return true;
                        ServerPlayer player = players.getPlayer(id);
                        //If you die again within 2Tick, this attachment will be invalidated
                        if (player != null && player != listener.player && !player.isDeadOrDying()) {
                            for (MobEffectInstance instance : effects) player.addEffect(instance);
                            return true;
                        }

                        //If the player has already exited, cancel this mission
                        //If it has not been reborn yet, continue the loop
                        return player == null || player != listener.player;
                    }, 2);
                }
            }
        }
    }

    @ApplyEvent
    public final void onInjure(HurtListener listener) {
        onHurt(listener);
    }

    @Override
    protected void additionalDebuff(LivingEntity target) {
        CalamityHelp.addIfDoesNotExist(target, 100, 1, MobEffects.POISON, MobEffects.WEAKNESS,
            CalamityEffects.ACID_VENOM.get(), CalamityEffects.ASTRAL_INFECTION.get(), CalamityEffects.CONFUSED.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("amalgam", 1).withStyle(ChatFormatting.GOLD));
        //Description from parent class
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "amalgamated_brain", 1, 2, 3,  4, 5);
        tooltips.add(CMLangUtil.getTranslatable("amalgam", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
