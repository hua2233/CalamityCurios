package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PreAttackListener extends HurtListener {
    @EventConstructor
    public PreAttackListener(ServerPlayer player, LivingHurtEvent event) {
        super(player, event);
    }

    @Override
    public void canceledEvent() {
        super.canceledEvent();
    }
}