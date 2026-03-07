package hua223.calamity.net.C2SPacket;

import hua223.calamity.capability.CalamityCapProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RageActive extends C2S {
    public RageActive() {
    }

    public RageActive(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(
            rage -> rage.activeRage(true, player));
    }
}
