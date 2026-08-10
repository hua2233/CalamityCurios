package hua223.calamity.util;

import hua223.calamity.events.LogoutRelease;
import hua223.calamity.main.AnnotationProcessor;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.objectweb.asm.Type;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.*;

//Upgrade From ConflictChain Class
public class CuriosConflictMap {
    private static final Int2ObjectOpenHashMap<ConflictNode> CONFLICT_MAPPING = new Int2ObjectOpenHashMap<>();

    public static void createConflictMap(AnnotationProcessor annotationProcessor) {
        final Map<Class<?>, Class<?>> classMap = new Object2ObjectOpenHashMap<>();

        annotationProcessor.addStartProcessingEntries(CurioRepel.class, processor -> {
            Class<?> base = processor.getDataClass();
            Map<String, Object> map = processor.getAnnotationData().annotationData();
            if (map.containsKey("isRoot")) CONFLICT_MAPPING.put(base.hashCode(), new ConflictNode(base));
            else {
                Object o = map.get("value");
                if (o == null) throw new IllegalStateException(base.getSimpleName() + " is neither a node nor a root system");
                classMap.put(base, processor.modLoader.loadClass(((Type) o).getClassName()));
            }
        });

        //flattening
        annotationProcessor.addPostProcessor(() -> {
            Class<?> node;
            for (Class<?> aClass : classMap.keySet()) {
                node = aClass;
                while (true) {
                    node = classMap.get(node);
                    ConflictNode conflictNode = CONFLICT_MAPPING.get(node.hashCode());
                    if (conflictNode != null) {
                        conflictNode.addNode(aClass, classMap.get(aClass));
                        CONFLICT_MAPPING.put(aClass.hashCode(), conflictNode);
                        break;
                    }
                }
            }

            CONFLICT_MAPPING.trim();
        });
    }

    public static boolean noOccupied(Item item, LivingEntity player) {
        int hash = item.getClass().hashCode();
        ConflictNode node = CONFLICT_MAPPING.get(hash);
        return node != null ? node.noOccupied(hash, player.getUUID()) : !CalamityHelp.hasCurio(player, item);
    }

    public static void lock(Item item, LivingEntity player, boolean lock) {
        int hash = item.getClass().hashCode();
        ConflictNode node = CONFLICT_MAPPING.get(hash);
        if (node != null) node.lock(hash, player.getUUID().hashCode(), lock);
    }

    @LogoutRelease
    public static void delete(ServerPlayer player) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isPresent()) {
            IItemHandlerModifiable modifiable = optional.get().getEquippedCurios();
            int playerCode = player.getUUID().hashCode();
            Set<ConflictNode> process = new ObjectOpenHashSet<>();
            for (int i = 0; i < modifiable.getSlots(); i++) {
                ItemStack stack = modifiable.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                int id = stack.getItem().getClass().hashCode();
                ConflictNode node = CONFLICT_MAPPING.get(id);
                if (node != null && !process.contains(node)) {
                    LongIterator iterable = node.curioSet.iterator();
                    while (iterable.hasNext()) {
                        long idDecode = iterable.nextLong() >> 32;
                        if (playerCode == idDecode) iterable.remove();
                    }
                    process.add(node);
                }
            }
        }
    }

    private static class ConflictNode {
        private final LongSet curioSet = new LongArraySet();
        private final Int2IntMap idMap = new Int2IntArrayMap();
        private final Int2ObjectArrayMap<IntList> reverse = new Int2ObjectArrayMap<>();

        public ConflictNode(Class<?> root) {
            int id = root.hashCode();
            idMap.put(id, id);
        }

        public void addNode(Class<?> node, Class<?> superNode) {
            int i = node.hashCode();
            if (idMap.containsKey(i)) throw new IllegalArgumentException("Duplicate node path!");
            int s = superNode.hashCode();
            idMap.put(i, s);
            IntList list = reverse.get(s);
            if (list == null) {
                list = new IntArrayList();
                reverse.put(s, list);
            }

            list.add(i);
        }

        private boolean noOccupied(int node, UUID uuid) {
            int id = uuid.hashCode();
            return !curioSet.contains(encoded(id, node)) && exploreDownwards(id, node);
        }

        private boolean exploreDownwards(int id, int node) {
            IntList list = reverse.get(node);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    int sub = list.getInt(i);
                    if (!exploreDownwards(id, sub) ||
                        curioSet.contains(encoded(id, sub))) return false;
                }
            }

            return true;
        }

        private void lock(int node, int id, boolean lock) {
            long e = encoded(id, node);
            if (lock) curioSet.add(e);
            else curioSet.remove(e);

            downwardChain(id, node, lock);
        }

        private void downwardChain(int id, int node, boolean lock) {
            IntList list = reverse.get(node);
            if (list == null) return;

            for (int i = 0; i < list.size(); i++) {
                int sub = list.getInt(i);
                downwardChain(id, sub, lock);
                long encoded = encoded(id, sub);
                if (lock) curioSet.add(encoded);
                else curioSet.remove(encoded);
            }
        }

        private long encoded(long id, long node) {
            return (id << 32) | (node & 0xFFFFFFFFL);
        }
    }
}
