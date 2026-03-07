package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
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

    @Override
    public void canceledEvent() {
        event.setResult(Event.Result.DENY);
        canceled = true;
    }
}
