package hua223.calamity.loots;

import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.register.items.CalamityItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;

import java.lang.invoke.*;
import java.util.*;

//Global loot function, used to manipulate all loot types in the context environment set.
//If simple modification or triggering of types is required, please use CalamityEventHandler to register curios events
public enum GlobalLoot {
    CHESTS_LOOTS {
        @Override
        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ServerPlayer player = (ServerPlayer) context.getParamOrNull(LootContextParams.THIS_ENTITY);
            return onDrops(new ChestLootContext(generatedLoot, context, player, context.getRandom()));
        }
    },

//    BLOCK_LOOTS {
//        @Override
//        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
//            return null;
//        }
//    },

    ENTITY_LOOTS {
        @Override
        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            Entity killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
            return onDrops(new EntitiesLootContext(generatedLoot, context, context.getRandom(), entity, killer));
        }
    };

    public abstract ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);

    public static boolean absoluteOperation;
    protected int playerCondition;
    protected final ArrayList<LootCall> globalLootFunctions = new ArrayList<>();


    public static void findLootFunction(AnnotationProcessor annotationProcessor) {
        final Map<Class<?>, GlobalLoot> typeMapping = new Object2ObjectOpenHashMap<>();
        annotationProcessor.addStartProcessingEntries(BaseLootContextPacker.GlobalLootType.class, processor -> {
            if (typeMapping.put(processor.getDataClass(), GlobalLoot.valueOf(
                ((ModAnnotation.EnumHolder) processor.getAnnotationData().annotationData().get("value")).getValue())) != null)
                throw new IllegalStateException("Do not allow registration of duplicate loot class");
        });

        final List<Object[]> list = new ArrayList<>();
        //如果你没有写入值，保持注解默认值true，则Forge不会保存它
        //如果它存在则认为非默认值，因为你设置了注解的onlyPlayer值
        annotationProcessor.addStartProcessingEntries(ApplyGlobalLoot.class,
            processor -> list.add(new Object[] {processor.getItemEnum(), processor.getMethodName(),
                processor.fromDescriptorGetType(), null, processor.getAnnotationData().annotationData().containsKey("onlyPlayer")}));

        annotationProcessor.addPostProcessor(() -> {
            //verification
            for (Object[] objects : list) {
                MethodType type = (MethodType) objects[2];
                Class<?>[] classes = type.parameterArray();
                if (classes.length == 1 && typeMapping.containsKey(classes[0]))  objects[3] = typeMapping.get(classes[0]);
                else throw new RuntimeException("The method parameter declared in the annotation is not a valid parameter type");
            }
        });

        annotationProcessor.onFmlSetup(() -> mountTo(list));
    }

    private static void mountTo(final List<Object[]> list) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType base = MethodType.methodType(void.class, BaseLootContextPacker.class);
        Map<GlobalLoot, List<LootCall>> conditionPlayer = new Object2ObjectOpenHashMap<>();

        for (Object[] objects : list) {
            Item item = ((CalamityItems) objects[0]).get();
            //Unlike dynamically added event calls, these are persistent。
            //Convert it into a lambda object without variable capture, achieving hard coded efficiency in performance
            MethodType covariantParameter = (MethodType) objects[2];
            try {
                LootCall call = (LootCall) LambdaMetafactory.metafactory(lookup, "doApply", MethodType.methodType(LootCall.class,
                    item.getClass()), base, lookup.findVirtual(item.getClass(), (String) objects[1], covariantParameter), covariantParameter).getTarget().invoke(item);
                //Allow it to detect the true object type
                GlobalLoot loot = (GlobalLoot) objects[3];
                if ((boolean) objects[4]) {
                    List<LootCall> calls = conditionPlayer.computeIfAbsent(loot, k -> new ArrayList<>());
                    calls.add(call);
                } else loot.globalLootFunctions.add(call);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        for (GlobalLoot loot : GlobalLoot.values()) {
            loot.playerCondition = loot.globalLootFunctions.size();
            if (conditionPlayer.containsKey(loot))
                loot.globalLootFunctions.addAll(conditionPlayer.get(loot));
        }
    }

    protected final ObjectArrayList<ItemStack> onDrops(BaseLootContextPacker packer) {
        for (int i = (packer.triggeredByPlayers() ? 0 : playerCondition); i < globalLootFunctions.size(); i++) {
            LootCall handle = globalLootFunctions.get(i);
            handle.doApply(packer);
            if (packer.isCancelDrop()) break;
        }

        return packer.generatedLoot;
    }

    protected interface LootCall {
        void doApply(BaseLootContextPacker context);
    }
}
