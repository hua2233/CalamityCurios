package hua223.calamity.util;

import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.register.items.CalamityItems;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.objectweb.asm.Type;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    @SuppressWarnings("unchecked")
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

        //Release processor
        IEventBus forgeBus = annotationProcessor.forgeBus;
        MethodHandles.Lookup lookup = annotationProcessor.lookup;

        annotationProcessor.onFmlSetup(() -> {
            try {
                MethodType interfaceType = MethodType.methodType(void.class, IMerchantWizard.class);
                String name = VillagerTrades.ItemListing.class.getMethods()[0].getName();

                Map<String, Object> VILLAGER_TRANSACTION_TAB = new Object2ObjectOpenHashMap<>();
                MethodType vanillaType = MethodType.methodType(MerchantOffer.class, Entity.class, RandomSource.class);

                for (Map.Entry<Object, List<Object[]>> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    List<Object[]> objectList = entry.getValue();

                    if (key instanceof String k) {
                        for (Object[] objects : objectList) {
                            MethodHandle handle = lookup.findVirtual((Class<?>) objects[3], (String) objects[2], vanillaType);
                            VillagerTrades.ItemListing listing = (VillagerTrades.ItemListing) LambdaMetafactory.metafactory(lookup, name, MethodType.methodType(VillagerTrades
                                .ItemListing.class, handle.type().parameterType(0)), vanillaType, handle, vanillaType).getTarget().invoke(((CalamityItems) objects[1]).get());

                            int level = Mth.clamp((Integer) objects[0], 1, 5);
                            String compositeKey = k + level;
                            ObjectArrayList<VillagerTrades.ItemListing> list = (ObjectArrayList<VillagerTrades.ItemListing>) VILLAGER_TRANSACTION_TAB.get(compositeKey);
                            if (list == null) VILLAGER_TRANSACTION_TAB.put(compositeKey, (list = new ObjectArrayList<>()));
                            list.add(listing);

                            byte[] levelInfo = (byte[]) VILLAGER_TRANSACTION_TAB.get(k);
                            if (levelInfo == null) VILLAGER_TRANSACTION_TAB.put(k, (levelInfo = new byte[] {0}));
                            levelInfo[0] |= (byte) (1 << level);
                        }
                    } else {
                        ITransaction[] table = new ITransaction[objectList.size()];
                        for (int i = 0; i < table.length; i++) {
                            Object[] objects = objectList.get(i);
                            MethodHandle handle = (MethodHandle) objects[0];
                            table[i] = (ITransaction) LambdaMetafactory.metafactory(lookup, "iss", MethodType.methodType(ITransaction.class,
                                handle.type().parameterType(0)), interfaceType, handle, (MethodType) objects[2]).getTarget().invoke(((CalamityItems) objects[1]).get());
                        }

                        ISS_TRANSACTION_TAB.put((Class<? extends IMerchantWizard>) key, table);
                    }
                }

                if (!VILLAGER_TRANSACTION_TAB.isEmpty()) {
                    Consumer<VillagerTradesEvent> consumer = event -> {
                        String k = event.getType().name();
                        Object levelInfo = VILLAGER_TRANSACTION_TAB.get(k);

                        if (levelInfo == null) return;
                        byte level = ((byte[]) levelInfo)[0];
                        Int2ObjectMap<List<VillagerTrades.ItemListing>> tradesMap = event.getTrades();

                        for (int i = 1; i < 6; i++) {
                            if ((level & 1 << i) != 0) {
                                String compositeKey = k + i;
                                tradesMap.get(i).addAll((ObjectArrayList<VillagerTrades.ItemListing>) VILLAGER_TRANSACTION_TAB.get(compositeKey));
                            }
                        }
                    };

                    forgeBus.addListener(consumer);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
