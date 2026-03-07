package hua223.calamity.net.S2CPacket;

import hua223.calamity.util.clientInfos.Sponge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SpongeData extends S2C {
    private int start = -1;
    private float value = -1;

    public SpongeData() {
    }

    public SpongeData(FriendlyByteBuf buf) {
        start = buf.readInt();
        value = buf.readFloat();
    }

    public SpongeData updateValue(float value) {
        this.value = value;
        return this;
    }

    public SpongeData updateStart(boolean start) {
        this.start = start ? 1 : 0;
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(start);
        byteBuf.writeFloat(value);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        if (start != -1) {
            if (start == 1) Sponge.startSpongeAndInit();
            else Sponge.closeSponge();
        } else if (value != -1) Sponge.setSpongeProgress(value);
    }
}
