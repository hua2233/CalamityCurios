package hua223.calamity.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class GlobalCuriosStorage {
    /**
     * Retrieves the Curios memory object for the specified player
     * 
     * @param storage The Curios storage interface instance to query
     * @param player The target player entity
     * @return CuriosMemory object if the corresponding storage memory exists, otherwise null
     */
    public static CuriosMemory getStorage(ICuriosStorage storage, LivingEntity player) {
        ObjectOpenHashSet<CuriosMemory> map = player.calamity$Player.Calamity$Player.curiosStorage;
        return map != null ? map.get(storage) : null;
    }

    /**
     * Safely removes Curios storage from the player
     * When the storage is successfully removed and the mapping set becomes empty,
     * the player's curiosStorage reference is set to null
     * 
     * @param entity The target living entity
     * @param storage The Curios storage interface instance to remove
     */
    @SuppressWarnings("ALL")
    public static void unEquipSafeRemove(LivingEntity entity, ICuriosStorage storage) {
        ObjectOpenHashSet<CuriosMemory> map = entity.calamity$Player.Calamity$Player.curiosStorage;
        if (map != null && map.remove(storage) && map.isEmpty())
            entity.calamity$Player.Calamity$Player.curiosStorage = null;
    }

    /**
     * Gets the count array from the player's Curios storage
     * 
     * @param player The target player entity
     * @param storage The storage object to query
     * @return The count array if storage exists, otherwise null
     */
    @SuppressWarnings("ALL")
    public static float[] getCountStorages(LivingEntity player, Object storage) {
        ObjectOpenHashSet<CuriosMemory> map = player.calamity$Player.Calamity$Player.curiosStorage;
        return map == null ? null : map.contains(storage) ? map.get(storage).count : null;
    }

    /**
     * Adds Curios storage to the player
     * In the vast majority of cases, each storage will only be placed once.
     * To prevent accidental overwriting, addOrGet is used to avoid duplicate placement
     * 
     * @param player The target player
     * @param storage The Curios storage interface instance to add
     */
    public static void addCurioStorage(Player player, ICuriosStorage storage) {
        CalamityPlayer calamityExpand = player.Calamity$Player;
        if (calamityExpand.curiosStorage == null)
            calamityExpand.curiosStorage = CalamityHelp.createMappingSet();
        calamityExpand.curiosStorage.addOrGet(new CuriosMemory(storage));
    }

    public static class CuriosMemory {

        /**
         * The BaseCurio instance that owns this memory record.
         * Used for equality checks and hash code computation to identify the associated curios item.
         */
        private final ICuriosStorage ownerCurio;
        /** Array storing count values for this curios */
        public final float[] count;
        /** Array storing UUID references for this curios */
        public final UUID[] uuids;
        /**
         * Storage reference holder. If pointing to 'this', indicates the entry is not enabled.
         * Otherwise holds either a single storage object or acts as a flag for multi-type storage.
         */
        //If it is itself, it proves that this entry is not enabled
        private Object object = this;
        /** Map for storing multiple storage types, keyed by their class types */
        private final Map<Class<?>, Object> map;

        /**
         * Constructs a new CuriosMemory instance for the given storage interface.
         * Initializes arrays and maps based on the storage configuration.
         *
         * @param storage The curios storage interface that owns this memory
         */
        private CuriosMemory(ICuriosStorage storage) {
            ownerCurio = storage;
            int countSize = storage.getCountSize();
            Class<?>[] multipleStorageTypes = storage.defineStorageType();
            if (storage.storageCount()) count = new float[countSize];
            else count = null;
            if (storage.storageID()) uuids = new UUID[countSize];
            else uuids = null;

            if (multipleStorageTypes == null) {
                map = null;
            } else if (multipleStorageTypes.length == 1) {
                map = null;
                object = null;
            } else {
                Map<Class<?>, Object> map = multipleStorageTypes.length > 0 ?
                    new Object2ObjectOpenHashMap<>(multipleStorageTypes.length) : null;

                if (map != null)
                    for (Class<?> c : multipleStorageTypes)
                        map.put(c, null);

                this.map = map;
            }
        }

        /**
         * Checks equality based on whether the compared object is the same as the owner curios.
         *
         * @param obj The object to compare against
         * @return true if the object is the same as ownerCurio, false otherwise
         */
        @Override
        @SuppressWarnings("ALL")
        public final boolean equals(Object obj) {
            return ownerCurio == obj;
        }

        /**
         * Computes the hash code for this CuriosMemory instance.
         * The hash code is derived from the ownerCurio's hash value to maintain consistency with equals().
         *
         * @return The hash code value based on the owning BaseCurio instance
         */
        @Override
        public final int hashCode() {
            return ownerCurio.hashCode();
        }

        /**
         * Retrieves a storage object of the specified type.
         * For single-type storage, returns the direct object reference.
         * For multi-type storage, looks up the type in the internal map.
         *
         * @param tClass The class type of the storage to retrieve
         * @param <T> The generic type parameter
         * @return The storage object of the requested type
         * @throws NoSuchElementException if the requested type is not registered
         */
        @SuppressWarnings("unchecked")
        public <T> T  getTypeStorage(Class<T> tClass) {
            if (object != this) {
                return (T) object;
            } else {
                if (map.containsKey(tClass))
                    return (T) map.get(tClass);
                else throw new NoSuchElementException("Unknown storage type! ->" + tClass.getSimpleName());
            }
        }

        /**
         * Stores an object in the appropriate storage location.
         * For single-type storage, sets the direct object reference.
         * For multi-type storage, places the object in the type-specific map entry.
         *
         * @param o The object to store (cannot be null or 'this')
         * @throws IllegalStateException if attempting to store null or the memory instance itself
         * @throws NoSuchElementException if the object's type is not registered in multi-type storage
         */
        public void putTypeStorage(Object o) {
            if (o == null || o == this)
                throw new IllegalStateException("cannot use null or itself as a storage value!");

            if (object != this) {
                object = o;
            } else {
                Class<?> tClass = o.getClass();
                if (map.containsKey(tClass)) map.put(tClass, o);
                else throw new NoSuchElementException("Unknown storage type! ->" + tClass.getSimpleName());
            }
        }
    }
}
