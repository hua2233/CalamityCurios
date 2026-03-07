package hua223.calamity.net.S2CPacket;

import hua223.calamity.util.RenderUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AstrErosionSync extends S2C {
    private final int value;

    public AstrErosionSync(FriendlyByteBuf buf) {
        value = buf.readVarInt();
    }

    public AstrErosionSync(int value) {
        this.value = value;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(value);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        RenderUtil.astrAmount = value;
    }
}
