package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ChangedDimensionListener extends BaseListener<PlayerEvent.PlayerChangedDimensionEvent> {
    public final ResourceKey<Level> to;
    public final ResourceKey<Level> from;
    public final ServerPlayer player;

    @EventConstructor
    public ChangedDimensionListener(PlayerEvent.PlayerChangedDimensionEvent event, ServerPlayer player) {
        super(event);
        this.player = player;
        from = event.getFrom();
        to = event.getTo();
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
