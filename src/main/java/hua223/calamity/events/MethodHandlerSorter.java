package hua223.calamity.events;

import hua223.calamity.events.listeners.BaseListener;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MethodHandlerSorter implements Comparable<MethodHandlerSorter> {
    public final Object holder;
    private final int priority;
    private final MethodHandle handle;
    private int reference;

    MethodHandlerSorter(Method method, Object eventHolder, int priority, MethodHandles.Lookup lookup) throws IllegalAccessException {
        this.holder = eventHolder;
        this.priority = priority;
        handle = lookup.unreflect(method).asType(MethodType.methodType(void.class, holder.getClass(), BaseListener.class)).bindTo(holder);

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
        return holder == obj || (obj instanceof MethodHandlerSorter sorter && sorter.holder== holder);
    }

    @Override
    public int hashCode() {
        return holder.hashCode();
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
