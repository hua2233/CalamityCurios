package hua223.calamity.net.S2CPacket;

import hua223.calamity.render.hud.FatigueHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class FatigueSync extends S2C {
    private float value;

    public FatigueSync(float value) {
        this.value = value;
    }

    public FatigueSync(boolean off) {
        value = off ? -1f : -2f;
    }

    public FatigueSync(FriendlyByteBuf buf) {
        value = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        if (value < 0) FatigueHud.notRender = value == -2f;
        else FatigueHud.syncPercentage(value);
    }
}
