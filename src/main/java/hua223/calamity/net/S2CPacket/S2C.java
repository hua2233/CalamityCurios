package hua223.calamity.net.S2CPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class S2C {
    public void processOnClient(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection().getReceptionSide().isClient()) {
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
