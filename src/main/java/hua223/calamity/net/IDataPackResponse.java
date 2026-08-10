package hua223.calamity.net;

import hua223.calamity.net.packets.ItemResponsePack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Data pack response interface for handling server-to-client (Item) packet communication.
 * Items implementing this interface can send NBT data packets to clients and handle client responses.
 */
//Server To Client
public interface IDataPackResponse {
    /**
     * NBT tag container for storing data to be sent.
     */
    CompoundTag PACK = new CompoundTag();

    /**
     * Sends a data packet containing NBT tag to the specified client player.
     *
     * @param player The target server player (receiver).
     */
    default void sendToClient(ServerPlayer player) {
        NetMessages.sendToClient(createNetPack(), player);
    }

    default void sendToAllClient() {
        NetMessages.sendToAllClient(createNetPack());
    }

    default DataPack createNetPack() {
        return new ItemResponsePack((Item) this, PACK);
    }

        /**
     * Gets the NBT tag container for storing data.
     * Calling this method clears all existing tag data in PACK.
     *
     * @return The cleared NBT tag container.
     */
    default CompoundTag getPack() {
        PACK.tags.clear();
        return PACK;
    }

    default void writeVec3(String key, Vec3 vec3, boolean clear) {
        if (clear) PACK.tags.clear();
        PACK.putDouble(key, (int) vec3.x);
        PACK.putDouble(key + 'y', (int) vec3.y);
        PACK.putDouble(key + 'z', (int) vec3.z);
    }

    default Vec3 readVec3(String key, CompoundTag tag) {
        return new Vec3(tag.getDouble(key), tag.getDouble(key + 'y'), tag.getDouble(key + 'z'));
    }

    /**
     * Callback method invoked when the client receives a data packet.
     * Only executes in client environment (Dist.CLIENT).
     *
     * @param tag The NBT tag data received from server, may be null.
     */
    @OnlyIn(Dist.CLIENT)
    void onClientResponse(CompoundTag tag);
}
