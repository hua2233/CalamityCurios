package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class CriticalHitTriggerListener extends BaseListener<CriticalHitEvent> {
    public static float singlePenetration;
    public final ServerPlayer player;
    public final LivingEntity target;

    @EventConstructor
    public CriticalHitTriggerListener(CriticalHitEvent event) {
        super(event);
        player = (ServerPlayer) event.getEntity();
        target = (LivingEntity) event.getTarget();
    }

    //仅在暴击判定成功后时添加修饰符，在此处执行耗时操作。这必定会应用
    public void applyAmplifier(float amplifier) {
        event.setDamageModifier(event.getDamageModifier() + amplifier);
    }

    public final void addSinglePenetration(float value) {
        singlePenetration += value;
        if (singlePenetration < 0)
            singlePenetration = 0;
    }

    @Override
    public void canceledEvent() {
        throw new UnsupportedOperationException("this event cannot be cancelled!");
    }

    @Override
    public boolean isCanceled() {
        return false;
    }
}
