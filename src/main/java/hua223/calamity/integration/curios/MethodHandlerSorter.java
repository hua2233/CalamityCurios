package hua223.calamity.integration.curios;

import hua223.calamity.integration.curios.listeners.BaseListener;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MethodHandlerSorter implements Comparable<MethodHandlerSorter> {
    public final Item owner;
    private final int priority;
    private final MethodHandle handle;
    private int reference;

    MethodHandlerSorter(Method method, Item item) {
        this.priority = method.getAnnotation(BaseCurio.ApplyEvent.class).value();
        owner = item;
        try {
            handle = MethodHandles.publicLookup().unreflect(method).asType(
                MethodType.methodType(void.class, Item.class, BaseListener.class)).bindTo(item);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
