package hua223.calamity.events;

import hua223.calamity.events.listeners.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CalamityPlayer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.jodah.typetools.TypeResolver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @LogoutRelease
    @SuppressWarnings("ALL")
    public static void removeBatch(ServerPlayer player) {
        if (player.Calamity$Player.getActiveEvents() == null) return;
        for (Map.Entry<EventTypes<?>, List<MethodHandlerSorter>> entry : player.Calamity$Player.getActiveEvents().entrySet()) {
            EventTypes types = entry.getKey();
            for (MethodHandlerSorter sorter : entry.getValue()) {
                sorter.pop();
                if (sorter.removable()) types.eventCaches.remove(sorter.holder);
            }
        }

    }

    private static EventTypes<?> fromClassGet(Class<?> clazz) {
        for (EventTypes<?> type : EVENT_TYPES)
            if (type.listenerClass == clazz)
                return type;

        throw new IllegalArgumentException("Event method with parameter type mismatch");
    }

    public static List<Method> collectEvents(Class<?> eventClass) {
        List<Method> methods = new ArrayList<>(4);
        int modifier = 17;
        for (Method method : eventClass.getMethods()) {
            if ((modifier & method.getModifiers()) == modifier
                && method.getReturnType() == void.class
                && method.isAnnotationPresent(ApplyEvent.class)) {
                methods.add(method);
            }
        }

        return methods;
    }

    @SuppressWarnings("ALL")
    public static void applyEvent(Object eventHolder, ServerPlayer player, final boolean apply) {
        try {
            Class<?> eClass = eventHolder.getClass();
            MethodHandles.Lookup lookup = eClass.isEnum() ? MethodHandles.privateLookupIn(
                eClass, MethodHandles.lookup()) : MethodHandles.publicLookup();

            for (Method method : collectEvents(eClass)) {
                //fromClassGet
                EventTypes<?> type = fromClassGet(method.getParameterTypes()[0]);
                MethodHandlerSorter sorter;

                if (type.eventCaches.contains(eventHolder)) sorter = type.eventCaches.get(eventHolder) ;
                else {
                    if (!apply) return;

                    sorter = new MethodHandlerSorter(method, eventHolder, method.getAnnotation(ApplyEvent.class).value(), lookup);
                    type.eventCaches.add(sorter);
                }

                if (apply) player.Calamity$Player.addEvent(type, sorter.push());
                else {
                    player.Calamity$Player.removeEvent(type, sorter.pop());
                    if (sorter.removable()) type.eventCaches.remove(eventHolder);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
