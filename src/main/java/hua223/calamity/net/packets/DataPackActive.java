package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class DataPackActive extends DataPack {
    private final Item handler;
    private final CompoundTag tag;

    public DataPackActive(Item handler) {
        this.handler = handler;
        tag = ((IKeyDataPackResponse) handler).getSerializationStream();
    }

    @SuppressWarnings("deprecation")
    public DataPackActive(FriendlyByteBuf buf) {
        handler = buf.readById(BuiltInRegistries.ITEM);
        tag = buf.readByte() == 0 ? buf.readNbt() : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeId(BuiltInRegistries.ITEM, handler);

        if (tag != null) {
            byteBuf.writeByte(0);
            byteBuf.writeNbt(tag);
        } else byteBuf.writeByte(1);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            if (tag != null) player.getPersistentData().put("CuriosKeyData", tag);
            ((IKeyDataPackResponse) handler).onServerResponse(player, tag);
        }
    }
}
