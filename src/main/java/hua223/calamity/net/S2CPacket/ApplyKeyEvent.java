package hua223.calamity.net.S2CPacket;

import hua223.calamity.register.keys.ClientInteraction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ApplyKeyEvent extends S2C {
    private final String type;
    private final boolean apply;

    public ApplyKeyEvent(String type, boolean apply) {
        this.type = type;
        this.apply = apply;
    }

    public ApplyKeyEvent(FriendlyByteBuf byteBuf) {
        type = byteBuf.readUtf();
        apply = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(type);
        byteBuf.writeBoolean(apply);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ClientInteraction.applyOrDelete(type, apply);
    }
}
