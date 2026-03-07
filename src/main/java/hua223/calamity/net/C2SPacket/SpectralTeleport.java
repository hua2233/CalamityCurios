package hua223.calamity.net.C2SPacket;

import hua223.calamity.integration.curios.item.SpectralVeil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class SpectralTeleport extends C2S {
    public SpectralTeleport() {}

    public SpectralTeleport(FriendlyByteBuf byteBuf) {}

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) SpectralVeil.teleport(player);
    }
}
