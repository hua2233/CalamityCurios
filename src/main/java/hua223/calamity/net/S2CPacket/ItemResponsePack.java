package hua223.calamity.net.S2CPacket;

import hua223.calamity.util.IDataPackResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class ItemResponsePack extends S2C {
    private final Item item;
    private final CompoundTag stream;

    public ItemResponsePack(Item item, CompoundTag stream) {
        this.item = item;
        this.stream = stream;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public ItemResponsePack(FriendlyByteBuf buf) {
        this.item = buf.readById(BuiltInRegistries.ITEM);
        stream = buf.readByte() == 0 ? buf.readNbt() : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeId(BuiltInRegistries.ITEM, item);
        if (stream != null) byteBuf.writeNbt(stream);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ((IDataPackResponse) item).onClientResponse(stream);
    }
}
