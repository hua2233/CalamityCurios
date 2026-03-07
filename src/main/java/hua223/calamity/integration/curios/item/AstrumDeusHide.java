package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class AstrumDeusHide extends BaseCurio implements ICuriosStorage {
    public AstrumDeusHide(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.ASTRAL_INFECTION.get(), CalamityEffects.CURSED_INFERNO.get());
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.source.isProjectile()) return;
        float[] arr = getCount(listener.player);

        if (arr != null && arr[0] == 0) {
            arr[0] = 1;
            for (int i = 0; i < 6; i++) {
                Meteor.of(listener.entity, listener.player, false);
            }

            DelayRunnable.addRunTask(600, () -> arr[0] = 0);
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving) {
            LivingEntity attacker = listener.entity;
            for (int i = 0; i < 4; i++) {
                Meteor.of(attacker, player, false);
            }

            float[] arr = getCount(listener.player);
            if (arr != null && arr[1] == 0) {
                player.invulnerableTime += 2;
                player.level.explode(player, player.getX(), player.getY(), player.getZ(),
                    2f, Explosion.BlockInteraction.NONE);


                arr[1] = 1;
                DelayRunnable.addRunTask(400, () -> arr[1] = 0);

                int tick = Math.min((int) listener.baseAmount * 3, 200);
                AttributeInstance instance = player.getAttribute(CalamityAttributes.CLOSE_RANGE.get());
                final AttributeModifier MODIFIER = new AttributeModifier(
                    UUID.randomUUID(), "astrum", 0.3, AttributeModifier.Operation.ADDITION);

                instance.addTransientModifier(MODIFIER);

                DelayRunnable.addRunTask(tick, () -> instance.removeModifier(MODIFIER));
            }

            attacker.hurt(new DamageSource("hideCounter"), listener.baseAmount * 0.75f);
            MobEffect effect = CalamityEffects.ASTRAL_INFECTION.get();
            if (!attacker.hasEffect(effect)) {
                attacker.addEffect(new MobEffectInstance(effect, 200, 0), player);
            }
        }
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("hide", 2));
            tooltips.add(CMLangUtil.getTranslatable("hide", 3));
            tooltips.add(CMLangUtil.getTranslatable("hide", 4));
            tooltips.add(CMLangUtil.getTranslatable("hide", 5));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("hide", 1));
        }
        return tooltips;
    }
}
