package hua223.calamity.net.S2CPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class PlayerVerticalSpeed extends S2C {
    private final float speed;

    public PlayerVerticalSpeed(float speed) {
        this.speed = speed;
    }

    public PlayerVerticalSpeed(FriendlyByteBuf buf) {
        speed = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(speed);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            player.calamity$VerticalSpeed += speed;
    }
}
