package hua223.calamity.integration.curios;

import hua223.calamity.events.CuriosEventHandler;
import hua223.calamity.integration.curios.listeners.*;
import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

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

    public static final EventTypes<CriticalHitListener> CRITICAL_HIT =
        new EventTypes<>(CriticalHitListener.class);

    private final Map<Item, MethodReference> eventCache = new Object2ObjectOpenHashMap<>(32);
    private final Class<?> listenerClass;
    private final MethodHandle supplier;

    public EventTypes(Class<T> listener) {
        this.listenerClass = listener;
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

    private static EventTypes<?> fromClassGet(Class<?> clazz) {
        for (EventTypes<?> type : EVENT_TYPES)
            if (type.listenerClass == clazz)
                return type;

        throw new IllegalStateException("Event method with parameter type mismatch");
    }

    public static int getEventTypeTotal() {
        return EVENT_TYPES.size();
    }

    @SuppressWarnings("unchecked")
    public T builderEvent(Object... args) throws Throwable {
        return (T) supplier.invokeExact(args);
    }

    public void removeBatch(List<CuriosEventHandler.MethodHandlerSorter> list) {
        for (CuriosEventHandler.MethodHandlerSorter sorter : list) {
            //multiplayerGame
            eventCache.computeIfPresent(sorter.item(),
                (k, v) -> --v.reference == 0 ? null : v);
        }
    }

    public static void applyEvent(Item curio, List<Method> methods, ServerPlayer player, final boolean apply) {
        for (Method method : methods) {
            EventTypes<?> type = fromClassGet(method.getParameterTypes()[0]);
            type.eventCache.compute(curio, (item, reference) -> {
                boolean v = reference == null;
                if (apply) {
                    if (v) reference = new MethodReference(method, curio);
                    CuriosEventHandler.addEvent(type, reference.push(),
                        method.getAnnotation(BaseCurio.ApplyEvent.class).value(), curio, player);
                } else if (!v) {
                    CuriosEventHandler.removeEvent(type, reference.pop(), player);
                    if (reference.reference == 0) reference = null;
                }

                return reference;
            });
        }
    }

    private static class MethodReference {
        private int reference;
        private final MethodHandle handle;

        private MethodReference(Method method, Item item) {
            try {
                handle = MethodHandles.publicLookup().unreflect(method).asType(
                    MethodType.methodType(void.class, Item.class, BaseListener.class)).bindTo(item);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private MethodHandle push() {
            reference++;
            return handle;
        }

        private MethodHandle pop() {
            reference--;
            return handle;
        }
    }
}
