package hua223.calamity.util;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.ItemResponsePack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

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
     * @param tag The NBT tag data to send, null if no additional data.
     */
    default void sendToClient(ServerPlayer player, CompoundTag tag) {
        NetMessages.sendToClient(new ItemResponsePack((Item) this, tag), player);
    }

    /**
     * Sends a data packet to the specified client player using data stored in PACK.
     * Sends null if PACK is empty.
     *
     * @param player The target server player (receiver).
     */
    default void sendToClient(ServerPlayer player) {
        sendToClient(player, PACK);
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

    /**
     * Callback method invoked when the client receives a data packet.
     * Only executes in client environment (Dist.CLIENT).
     *
     * @param tag The NBT tag data received from server, may be null.
     */
    @OnlyIn(Dist.CLIENT)
    void onClientResponse(CompoundTag tag);
}
