package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class CriticalHitListener extends BaseListener<CriticalHitEvent> {
    public static float singlePenetration;
    private final List<Runnable> callBack;
    public float probability;
    private boolean inCallBack;
    public final ServerPlayer player;
    public final LivingEntity target;

    @EventConstructor
    public CriticalHitListener(CriticalHitEvent event) {
        super(event);
        player = (ServerPlayer) event.getEntity();
        target = (LivingEntity) event.getTarget();
        callBack = new ArrayList<>();
    }

    public boolean isCriticalHit() {
        return event.getResult() == Event.Result.ALLOW || event.isVanillaCritical();
    }

    public void setCriticalHit() {
        if (inCallBack) return;
        event.setResult(Event.Result.ALLOW);
    }

    public void applyAmplifier(float amplifier) {
        event.setDamageModifier(event.getDamageModifier() + amplifier);
    }

    public void addCallbackAfterCriticalHit(Runnable consumer) {
        if (inCallBack) CalamityCurios.LOGGER.warn("It is not allowed to add new callback methods in the callback process!");
        callBack.add(consumer);
    }

    public void applyCallBack() {
        boolean criticalHit = isCriticalHit();
        if (!criticalHit && probability > 0 && !isCanceled() &&
            (probability >= 1f || player.getRandom().nextFloat() < probability)) {
            event.setResult(Event.Result.ALLOW);
            criticalHit = true;
        }

        if (criticalHit && !callBack.isEmpty()) {
            inCallBack = true;
            for (Runnable back : callBack) back.run();
        }
    }

    public void addSinglePenetration(float value) {
        singlePenetration += value;
        if (singlePenetration < 0)
            singlePenetration = 0;
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
