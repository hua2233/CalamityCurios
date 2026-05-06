package hua223.calamity.integration.curios.listeners;

import net.minecraftforge.eventbus.api.Event;

public abstract class BaseListener<T extends Event> {
    protected final T event;

    protected BaseListener(T event) {
        this.event = event;
    }

    public boolean isCanceled() {
        return event.isCanceled();
    }

    public abstract void canceledEvent();
}
