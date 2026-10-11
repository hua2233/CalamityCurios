package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public non-sealed class HurtListener extends HDListener<LivingHurtEvent> {
    private static DamageSource replace;

    @EventConstructor
    public HurtListener(LivingHurtEvent event, ServerPlayer player) {
        super(event, player, event.getAmount(), event.getSource());
    }

    protected HurtListener(ServerPlayer player, LivingHurtEvent event) {
        super(player, event, event.getAmount(), event.getSource());
    }

    public static DamageSource trySetSource(DamageSource source) {
        if (replace != null) {
            source = replace;
            replace = null;
        }

        return source;
    }

    public void setSource(DamageSource source) {
        replace = source;
    }
}
