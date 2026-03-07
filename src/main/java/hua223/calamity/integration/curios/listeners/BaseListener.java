package hua223.calamity.integration.curios.listeners;

import hua223.calamity.events.CuriosEventHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public abstract class BaseListener<T extends Event> {
    public final T event;

    protected BaseListener(T event) {
        this.event = event;
    }

    public boolean isCanceled() {
        return event.isCanceled();
    }

    public void preprocessing(List<CuriosEventHandler.MethodHandlerSorter> list, Player player) {}

    public abstract void canceledEvent();
}
