package hua223.calamity.net.C2SPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class C2S {
    public void processOnServer(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection().getReceptionSide().isServer()) {
            ctx.enqueueWork(() -> {
                handler(ctx);
                ctx.setPacketHandled(true);
            });
        }
    }

    public abstract void handler(NetworkEvent.Context context);

    public void toBytes(FriendlyByteBuf byteBuf) {
    }
}
