package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

public class CriticalHitCheckListener extends BaseListener<CriticalHitEvent> {
    public float probability;
    public final ServerPlayer player;
    public final LivingEntity target;

    @EventConstructor
    public CriticalHitCheckListener(CriticalHitEvent event) {
        super(event);
        player = (ServerPlayer) event.getEntity();
        target = (LivingEntity) event.getTarget();
    }

    public boolean isCriticalHit() {
        if (event.getResult() != Event.Result.DEFAULT)
            return event.getResult() == Event.Result.ALLOW;

        if (event.isVanillaCritical()) {
            if (probability < 0 && player.getRandom().nextFloat() < probability + 1) {
                event.setResult(Event.Result.DENY);
                return false;
            }

            return true;
        } else if (probability > 0 && (probability >= 1f || player.getRandom().nextFloat() < probability)){
            event.setResult(Event.Result.ALLOW);
            return true;
        }

        return false;
    }

    //在暴击判定时添加修饰符，通常是直接添加，执行不那么耗时的操作。如果暴击判定不通过则不会应用
    public void applyAmplifier(float amplifier) {
        event.setDamageModifier(event.getDamageModifier() + amplifier);
    }

    public void setCriticalHit() {
        event.setResult(Event.Result.ALLOW);
    }

    @Override
    public void canceledEvent() {
        event.setResult(Event.Result.DENY);
    }

    @Override
    public boolean isCanceled() {
        return event.getResult() == Event.Result.DENY;
    }
}
