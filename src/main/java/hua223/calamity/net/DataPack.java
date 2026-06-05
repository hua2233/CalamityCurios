package hua223.calamity.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class DataPack {
    protected final void processOnSide(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() == getClass().getAnnotation(
            CommunicationDirection.class).value())
            ctx.enqueueWork(() -> handler(ctx));
        ctx.setPacketHandled(true);
    }

    protected abstract void handler(NetworkEvent.Context context);

    protected void toBytes(FriendlyByteBuf byteBuf) {};
}
