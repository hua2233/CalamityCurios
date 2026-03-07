package hua223.calamity.net.S2CPacket;

import hua223.calamity.render.hud.FatigueSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class FatigueDataSync extends S2C {
    private int render = -1;
    private int value = -1;

    public FatigueDataSync() {
    }

    public FatigueDataSync(FriendlyByteBuf buf) {
        render = buf.readInt();
        value = buf.readInt();
    }

    public FatigueDataSync setRender(boolean render) {
        this.render = render ? 0 : 1;
        return this;
    }

    public FatigueDataSync setProgress(int value) {
        this.value = value;
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(render);
        byteBuf.writeInt(value);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        if (render != -1) FatigueSlot.notRender = !(render == 0);
        if (value != -1) FatigueSlot.setProgress(value);
    }
}
