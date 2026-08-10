package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.net.IDataPackResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Item Response Packet Class
 * Used to send item NBT data response from server to client
 * Extends S2C base class to implement server-to-client one-way communication
 */
@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class ItemResponsePack extends DataPack {
    private final Item item;
    private final CompoundTag stream;

    /**
     * Constructor: Create item response packet
     *
     * @param item The item object to transmit
     * @param stream The NBT tag data of the item, containing additional item information
     */
    public ItemResponsePack(@NotNull Item item, @NotNull CompoundTag stream) {
        this.item = item;
        this.stream = stream;
    }

    /**
     * Constructor: Read and deserialize packet from network buffer
     * Only called in client environment
     *
     * @param buf Network data buffer containing serialized item and NBT data
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public ItemResponsePack(FriendlyByteBuf buf) {
        item = buf.readById(BuiltInRegistries.ITEM);
        stream = buf.readNbt();
    }

    /**
     * Serialize the packet to network buffer
     * First writes the item's registry ID, then writes NBT data (if exists)
     *
     * @param byteBuf Target network data buffer
     */
    @Override
    @SuppressWarnings("deprecation")
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeId(BuiltInRegistries.ITEM, item);
        byteBuf.writeNbt(stream);
    }

    /**
     * Handle the received packet
     * Executes on client side, calls the IDataPackResponse interface method of the item to process response data
     *
     * @param context Network event context, providing packet processing environment information
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        ((IDataPackResponse) item).onClientResponse(stream);
    }
}
