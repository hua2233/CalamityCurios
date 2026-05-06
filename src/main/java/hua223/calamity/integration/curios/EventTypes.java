package hua223.calamity.integration.curios;

import hua223.calamity.events.CuriosEventHandler;
import hua223.calamity.integration.curios.listeners.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventTypes<T extends BaseListener<?>> {
    private static final List<EventTypes<?>> EVENT_TYPES = new ArrayList<>();

    public static final EventTypes<HurtListener> HURT =
        new EventTypes<>(HurtListener.class);

    public static final EventTypes<PlayerHealListener> HEAL =
        new EventTypes<>(PlayerHealListener.class);

    public static final EventTypes<DeathListener> DEATH =
        new EventTypes<>(DeathListener.class);

    public static final EventTypes<EffectListener> EFFECT =
        new EventTypes<>(EffectListener.class);

    public static final EventTypes<PlayerAttackListener> ATTACK =
        new EventTypes<>(PlayerAttackListener.class);

    public static final EventTypes<ProjectileHitListener> PROJECTILE_HIT =
        new EventTypes<>(ProjectileHitListener.class);

    public static final EventTypes<ProjectileSpawnListener> PROJECTILE_SPAWN =
        new EventTypes<>(ProjectileSpawnListener.class);

    public static final EventTypes<ChangedDimensionListener> DIMENSION_CHANGE =
        new EventTypes<>(ChangedDimensionListener.class);

    public static final EventTypes<CriticalHitCheckListener> CRITICAL_HIT_CHECK =
        new EventTypes<>(CriticalHitCheckListener.class);

    public static final EventTypes<CriticalHitTriggerListener> CRITICAL_HIT_TRIGGER =
        new EventTypes<>(CriticalHitTriggerListener.class);

//    private final Map<Item, MethodReference> eventCache = new Object2ObjectOpenHashMap<>(32);
    private final ObjectOpenHashSet<MethodHandlerSorter> eventCaches = CalamityHelp.createMappingSet();
    public final Class<T> listenerClass;
    private final MethodHandle supplier;

    public EventTypes(Class<T> listener) {
        listenerClass = listener;
        //ResolveConstructorFunction
        try {
            for (Constructor<?> constructor : listener.getConstructors())
                if (constructor.isAnnotationPresent(EventConstructor.class)) {
                    MethodHandle original = MethodHandles.publicLookup().unreflectConstructor(constructor);
                    supplier = original.asType(MethodType.methodType(BaseListener.class, original.type()))
                        .asSpreader(Object[].class, constructor.getParameterCount());

                    //verifyParameters
                    for (Class<?> c : constructor.getParameterTypes()) if (c.isPrimitive()) CalamityCurios.LOGGER.warn(
                        "Basic data type found in event constructor: {}, corresponding wrapper data type can be manually set", c.getSimpleName());
                    EVENT_TYPES.add(this);
                    return;
                }

            throw new NoSuchMethodException("No suitable constructor found in target class: " + listener.getSimpleName());
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getEventTypeTotal() {
        return EVENT_TYPES.size();
    }

    @SuppressWarnings("unchecked")
    public T builderEvent(Object... args) throws Throwable {
        return (T) supplier.invokeExact(args);
    }

    @SuppressWarnings("ALL")
    public void removeBatch(List<MethodHandlerSorter> list) {
        for (MethodHandlerSorter sorter : list) {
            sorter.pop();
            if (sorter.removable()) eventCaches.remove(sorter.owner);
        }
    }

    private static EventTypes<?> fromClassGet(Class<?> clazz) {
        for (EventTypes<?> type : EVENT_TYPES)
            if (type.listenerClass == clazz)
                return type;

        throw new IllegalStateException("Event method with parameter type mismatch");
    }

    @SuppressWarnings("ALL")
    public static void applyEvent(Item curio, List<Method> methods, ServerPlayer player, final boolean apply) {
        for (Method method : methods) {
            //fromClassGet
            EventTypes<?> type = fromClassGet(method.getParameterTypes()[0]);
            MethodHandlerSorter sorter;

            if (type.eventCaches.contains(curio)) sorter = type.eventCaches.get(curio) ;
            else {
                if (!apply) return;

                sorter = new MethodHandlerSorter(method, curio);
                type.eventCaches.add(sorter);
            }

            if (apply) CuriosEventHandler.addEvent(type, sorter.push(), player);
            else {
                CuriosEventHandler.removeEvent(type, sorter.pop(), player);
                if (sorter.removable()) type.eventCaches.remove(curio);
            }
        }
    }
}
