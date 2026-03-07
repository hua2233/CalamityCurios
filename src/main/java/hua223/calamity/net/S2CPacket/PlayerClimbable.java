package hua223.calamity.net.S2CPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class PlayerClimbable extends S2C {
    private final boolean can;

    public PlayerClimbable(boolean can) {
        this.can = can;
    }

    public PlayerClimbable(FriendlyByteBuf buf) {
        can = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(can);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        Minecraft.getInstance().player.calamity$CanClimbable(can);
    }
}
