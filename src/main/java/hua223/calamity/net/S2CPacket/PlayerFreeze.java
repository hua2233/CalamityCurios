package hua223.calamity.net.S2CPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class PlayerFreeze extends S2C {
    private final boolean freeze;
    public PlayerFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    public PlayerFreeze(FriendlyByteBuf buf) {
        freeze = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(freeze);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) player.calamity$IsFreeze = freeze;
    }
}
