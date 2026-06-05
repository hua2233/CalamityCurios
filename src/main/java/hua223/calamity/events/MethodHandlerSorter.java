package hua223.calamity.events;

import hua223.calamity.events.listeners.BaseListener;
import hua223.calamity.register.gui.SpellType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MethodHandlerSorter implements Comparable<MethodHandlerSorter> {
    public final Object owner;
    private final int priority;
    private final MethodHandle handle;
    private int reference;

    MethodHandlerSorter(Method method, Object owner, int priority) {
        this.owner = owner;
        this.priority = priority;
        try {
            boolean isSpell = owner instanceof SpellType;
            MethodHandles.Lookup lookup = isSpell ? MethodHandles.privateLookupIn(
                owner.getClass(), MethodHandles.lookup()) : MethodHandles.publicLookup();
            handle = lookup.unreflect(method).asType(
                MethodType.methodType(void.class, isSpell ? SpellType.class : Item.class, BaseListener.class)).bindTo(owner);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    MethodHandlerSorter(MethodHandle handle, Object owner, int priority) {
        this.owner = owner;
        this.priority = priority;
        this.handle = handle;
    }

    MethodHandlerSorter push() {
        reference++;
        return this;
    }

    MethodHandlerSorter pop() {
        reference--;
        return this;
    }

    @Override
    @SuppressWarnings("ALL")
    public boolean equals(Object obj) {
        return owner == obj || (obj instanceof MethodHandlerSorter sorter && sorter.owner == owner);
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    public boolean removable() {
        return reference == 0;
    }

    public MethodHandle invoke() {
        return handle;
    }

    @Override
    public int compareTo(@NotNull MethodHandlerSorter sorter) {
        return Integer.compare(priority, sorter.priority);
    }
}
