package hua223.calamity.net.S2CPacket;

import hua223.calamity.render.entity.CrystallizationRenderLayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CrystallizationData extends S2C {
    public final int state;

    public CrystallizationData(int state) {
        this.state = state;
    }

    public CrystallizationData(FriendlyByteBuf buf) {
        state = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(state);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        switch (state) {
            case 0 -> CrystallizationRenderLayer.start();
            case 1 -> CrystallizationRenderLayer.startChange();
            case 2 -> CrystallizationRenderLayer.notStopChange();
            case 3 -> CrystallizationRenderLayer.stop();
        }
    }
}
