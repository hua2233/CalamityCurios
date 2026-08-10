package hua223.calamity.util;

import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.register.items.CalamityItems;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import org.objectweb.asm.Type;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ITransaction {
    Map<Class<?>, ITransaction[]> ISS_TRANSACTION_TAB = new Object2ObjectOpenHashMap<>();

    static void fromTableFill(IMerchantWizard wizard) {
        ITransaction[] transactions = ISS_TRANSACTION_TAB.get(wizard.getClass());
        if (transactions != null)
            for (ITransaction transaction : transactions)
                transaction.iss(wizard);
    }

    void iss(IMerchantWizard wizard);

    static void findTransactionList(AnnotationProcessor annotationProcessor) {
        final Map<Object, List<Object[]>> map = new Object2ObjectOpenHashMap<>();
        annotationProcessor.addStartProcessingEntries(ItemSupplier.class, processor -> {
            Class<?> tradAble = processor.getDataClass();
            Map<String, Object> data = processor.getAnnotationData().annotationData();

            CalamityItems item = processor.getItemEnum(data, tradAble.getSimpleName());
            if (data.containsKey("ISSMerchant")) {
                Class<?> wizard = processor.modLoader.loadClass(((Type) data.get("ISSMerchant")).getClassName());
                MethodType covariantParameter = MethodType.methodType(void.class, wizard);
                MethodHandle handle = processor.lookup.findVirtual(tradAble, processor.getMethodName(), covariantParameter);
                map.computeIfAbsent(wizard, k -> new ArrayList<>()).add(new Object[] {handle, item, covariantParameter});
            } else if (data.containsKey("villagerSupplier") && data.containsKey("villagerLevel")) {
                String profession = (String) data.get("villagerSupplier");
                Integer level = (Integer) data.get("villagerLevel");
                map.computeIfAbsent(profession, k -> new ArrayList<>()).add(new Object[] {level, item, processor.getMethodName(), processor.getDataClass()});
            } else throw new IllegalClassFormatException(item.getClass().getSimpleName() + "There are invalid annotation transaction methods within the class");
        });

        annotationProcessor.onFmlSetup(() -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodType interfaceType = MethodType.methodType(void.class, IMerchantWizard.class);
                for (Map.Entry<Object, List<Object[]>> entry : map.entrySet()) {
                    if (entry.getKey() instanceof Class<?> clazz) {
                        List<Object[]> objectList = entry.getValue();
                        ITransaction[] table = new ITransaction[objectList.size()];

                        for (int i = 0; i < table.length; i++) {
                            Object[] objects = objectList.get(i);
                            MethodHandle handle = (MethodHandle) objects[0];
                            table[i] = (ITransaction) LambdaMetafactory.metafactory(lookup, "iss", MethodType.methodType(ITransaction.class,
                                handle.type().parameterType(0)), interfaceType, handle, (MethodType) objects[2]).getTarget().invoke(((CalamityItems) objects[1]).get());
                        }

                        ISS_TRANSACTION_TAB.put(clazz, table);
                    }
                }

                for (Class<?> aClass : ISS_TRANSACTION_TAB.keySet()) map.remove(aClass);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        annotationProcessor.addPostProcessor(() -> {
            Consumer<VillagerTradesEvent> consumer = event -> {
                List<Object[]> objectList = map.get(event.getType().name());
                if (objectList != null) {
                    MethodType vanillaType = MethodType.methodType(MerchantOffer.class, Entity.class, RandomSource.class);

                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    String name = VillagerTrades.ItemListing.class.getMethods()[0].getName();
                    Int2ObjectMap<List<VillagerTrades.ItemListing>> int2ObjectMap = event.getTrades();

                    for (Object[] objects : objectList) {
                        try {
                            MethodHandle handle = lookup.findVirtual((Class<?>) objects[3], (String) objects[2], vanillaType);
                            int2ObjectMap.get((int) objects[0]).add((VillagerTrades.ItemListing) LambdaMetafactory.metafactory(lookup, name,
                                MethodType.methodType(VillagerTrades.ItemListing.class, handle.type().parameterType(0)),
                                vanillaType, handle, vanillaType).getTarget().invoke(((CalamityItems) objects[1]).get()));
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };

            annotationProcessor.forgeBus.addListener(consumer);
        });
    }

    @SuppressWarnings("all")
    static <T> void add(Map<T, List<CalamityItems>> table, T o, CalamityItems item) {
        List<CalamityItems> items = table.get(o);
        if (items == null) {
            items = new ArrayList<>();
            table.put(o, items);
        }
        items.add(item);
    }
}
