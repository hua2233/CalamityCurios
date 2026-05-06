package hua223.calamity.util;

import hua223.calamity.integration.curios.BaseCurio;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Global storage manager for Curios items across all players.
 * Maintains a mapping between player UUIDs and their associated Curios memory data,
 * providing methods to add, remove, and query curios storage information.
 */
public class GlobalCuriosStorage {
    /**
     * Static map storing Curios memory data indexed by player UUID.
     * Each entry maps a player's UUID to a set of their equipped curios memories.
     */
    private static final Map<UUID, ObjectOpenHashSet<CuriosMemory>> STORAGES =
        new Object2ObjectOpenHashMap<>(2);

    /**
     * Retrieves the Curios memory associated with a specific storage interface for a living entity.
     *
     * @param storage The curios storage interface to look up
     * @param player The living entity whose curios memory is being queried
     * @return The CuriosMemory instance if found, null otherwise
     */
    public static CuriosMemory getStorage(ICuriosStorage storage, LivingEntity player) {
        ObjectOpenHashSet<CuriosMemory> map = STORAGES.get(player.getUUID());

        return map != null ? map.get(storage) : null;
    }

    /**
     * Safely removes a curios storage entry from an entity's storage without throwing exceptions.
     * If the storage set becomes empty after removal, the entire entry is cleaned up.
     *
     * @param entity The entity from which to remove the storage
     * @param storage The curios storage interface to remove
     */
    @SuppressWarnings("ALL")
    public static void unEquipSafeRemove(Entity entity, ICuriosStorage storage) {
        UUID uuid = entity.getUUID();
        if (STORAGES.containsKey(uuid)) {
            ObjectOpenHashSet<CuriosMemory> set = STORAGES.get(uuid);
            set.remove(storage);
            if (set.isEmpty()) STORAGES.remove(uuid);
        }
    }

    /**
     * Retrieves the count array from storages for a given entity and storage object.
     * The storage object must implement the ICuriosStorage interface.
     *
     * @param player The entity whose storage counts are being queried
     * @param storage The storage object (must implement ICuriosStorage)
     * @return The float array containing storage counts, or null if not found
     */
    public static float[] getCountStorages(Entity player, Object storage) {
        UUID id = player.getUUID();
        if (STORAGES.containsKey(id)) {
            CuriosMemory memory = STORAGES.get(player.getUUID()).get(storage);
            if (memory != null) return memory.count;
        }
        return null;
    }

    /**
     * Removes all storage data associated with a specific player.
     *
     * @param player The player whose storage data should be removed
     */
    public static void removePlayerStorage(Player player) {
        STORAGES.remove(player.getUUID());
    }

    /**
     * Adds a new curios storage entry for a player, or retrieves the existing one if already present.
     * Uses computeIfAbsent to prevent accidental overwriting in cases where multiple additions occur.
     * In most cases, each curios will only be added once.
     *
     * @param player The player to whom the curios storage belongs
     * @param storage The curios storage interface to add
     */
    //In the vast majority of cases, it will only be placed once.
    //To prevent accidental overwriting, addOrGet is used to cancel the placement again
    public static void addCurioStorage(Player player, ICuriosStorage storage) {
        STORAGES.computeIfAbsent(player.getUUID(), k ->
            CalamityHelp.createMappingSet()).addOrGet(new CuriosMemory(storage));
    }

    /**
     * Removes a specific curios storage entry from a player's storage set.
     *
     * @param player The player whose storage should be modified
     * @param storage The curios storage interface to remove
     */
    @SuppressWarnings("ALL")
    public static void removeCurioStorage(Player player, ICuriosStorage storage) {
        STORAGES.get(player.getUUID()).remove(storage);
    }

    /**
     * Represents the memory state of a single curios item, including its storage data,
     * type mappings, and associated metadata such as counts and UUIDs.
     */
    public static class CuriosMemory {

        /**
         * The BaseCurio instance that owns this memory record.
         * Used for equality checks and hash code computation to identify the associated curios item.
         */
        private final BaseCurio ownerCurio;
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
            ownerCurio = (BaseCurio) storage;
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
