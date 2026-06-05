package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;

public class EffectListener extends BaseListener<MobEffectEvent.Applicable> {
    public final ServerPlayer player;
    public final MobEffectInstance instance;
    public final MobEffect effect;
    private boolean canceled;

    @EventConstructor
    public EffectListener(MobEffectEvent.Applicable event, ServerPlayer player) {
        super(event);
        this.player = player;
        this.instance = event.getEffectInstance();
        this.effect = instance.getEffect();
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    public void tryCancel(MobEffect... effects) {
        for (MobEffect effect : effects)
            if (effect == this.effect) {
                canceledEvent();
                return;
            }
    }

    public void tryCancelHarmfulOnes(float probability) {
        if (!effect.isBeneficial() && player.getRandom().nextFloat() < probability)
            canceledEvent();
    }

    public void setEffectDuration(int duration) {
        setEffectProperties(duration, instance.getAmplifier());
    }

    public void setEffectAmplifier(int amplifier) {
        setEffectProperties(instance.getDuration(), amplifier);
    }

    //This event occurs before the server sends the package. Simply set the properties without manually synchronizing
    public void setEffectProperties(int duration, int amplifier) {
        instance.calamity$SetProperties(duration, amplifier, null);
    }

    @Override
    public void canceledEvent() {
        event.setResult(Event.Result.DENY);
        canceled = true;
    }
}
