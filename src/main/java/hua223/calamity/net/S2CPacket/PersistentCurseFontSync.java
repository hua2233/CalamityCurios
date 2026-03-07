package hua223.calamity.net.S2CPacket;

import hua223.calamity.capability.EnchantmentProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class PersistentCurseFontSync extends S2C {
    private final List<FontInfo> fontInfos;

    public PersistentCurseFontSync(int preSize) {
        fontInfos = new ArrayList<>(preSize);
    }

    public void addSync(Item stack, boolean gradual, int start, int end, int semiCycle) {
        fontInfos.add(new FontInfo(stack, gradual, start, end, semiCycle));
    }

    @OnlyIn(Dist.CLIENT)
    public PersistentCurseFontSync(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        fontInfos = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
            fontInfos.add(FontInfo.deserialization(buf));
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(fontInfos.size());
        fontInfos.forEach(fontInfo -> fontInfo.serializationTo(byteBuf));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        for (FontInfo info : fontInfos) EnchantmentProvider.addStyleProvider(
            info.item, info.gradual, info.start, info.end, info.cycle, false);
    }

    @SuppressWarnings("deprecation")
    private record FontInfo(Item item, boolean gradual, int start, int end, int cycle) {
        public void serializationTo(FriendlyByteBuf byteBuf) {
            byteBuf.writeId(Registry.ITEM, item);
            byteBuf.writeBoolean(gradual);
            byteBuf.writeInt(start);
            byteBuf.writeInt(end);
            byteBuf.writeInt(cycle);
        }

        public static FontInfo deserialization(FriendlyByteBuf buf) {
            return new FontInfo(buf.readById(Registry.ITEM), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt());
        }
    }
}
