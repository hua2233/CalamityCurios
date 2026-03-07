package hua223.calamity.net.S2CPacket;

import hua223.calamity.integration.curios.item.GravistarSabaton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class PlayerJumpPower extends S2C {
    private final float value;

    public PlayerJumpPower(FriendlyByteBuf buf) {
        value = buf.readFloat();
    }

    public PlayerJumpPower(float value) {
        this.value = value;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        if (Minecraft.getInstance().player != null)
            GravistarSabaton.jumpPower += value;
    }
}
