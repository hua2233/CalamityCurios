package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class AstrumDeusHide extends BaseCurio implements ICuriosStorage {
    public AstrumDeusHide(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getMemory(player).putTypeStorage(player.level().damageSources().indirectMagic(player, player));
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) {
            getUUID(equipped)[0] = uuid;
            modifier.put(CalamityAttributes.CLOSE_RANGE.get(),
                new VariableAttributeModifier(uuid, "hide", 0, AttributeModifier.Operation.ADDITION));
        }
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.ASTRAL_INFECTION.get(), CalamityEffects.CURSED_INFERNO.get());
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.isFarAttack()) return;

        ItemCooldowns cooldowns = listener.player.getCooldowns();
        if (!cooldowns.isOnCooldown(this)) {
            for (int i = 0; i < 6; i++)
                Meteor.of(listener.entity, listener.player, false);

            cooldowns.addCooldown(this, 600);
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            ServerPlayer player = listener.player;
            LivingEntity attacker = listener.entity;

            ItemCooldowns cooldowns = listener.player.getCooldowns();
            if (!cooldowns.isOnCooldown(this)) {
                for (int i = 0; i < 4; i++)
                    Meteor.of(attacker, player, false);

                player.calamity$SetInvulnerableTime(2);
                player.level().explode(player, player.getX(), player.getY(), player.getZ(),
                    2f, Level.ExplosionInteraction.NONE);
                cooldowns.addCooldown(this, 400);

                int tick = Math.min((int) listener.baseAmount * 3, 200);
                AttributeInstance instance = player.getAttribute(CalamityAttributes.CLOSE_RANGE.get());
                VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(getFirstUUID(player));
                modifier.setValue(0.3, instance);
                DelayRunnable.addRunTask(tick, () -> modifier.setValue(0, instance));
            }

            attacker.hurt(getMemory(listener.player).getTypeStorage(DamageSource.class), listener.baseAmount * 0.75f);
            MobEffect effect = CalamityEffects.ASTRAL_INFECTION.get();
            if (!attacker.hasEffect(effect))
                attacker.addEffect(new MobEffectInstance(effect, 200, 0), player);
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
    public Class<?>[] defineStorageType() {
        return new Class[] {DamageSource.class};
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "hide", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("hide", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
