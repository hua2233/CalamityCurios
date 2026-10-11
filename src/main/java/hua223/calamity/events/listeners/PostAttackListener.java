package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class PostAttackListener extends DamageListener {
    public final boolean isCalamityCriticalHits;
    @EventConstructor
    public PostAttackListener(ServerPlayer player, LivingDamageEvent event,  Boolean calamityCriticalHits) {
        super(player, event);
        isCalamityCriticalHits = calamityCriticalHits;
    }
}
