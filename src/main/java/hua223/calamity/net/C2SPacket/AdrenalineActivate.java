package hua223.calamity.net.C2SPacket;


import hua223.calamity.capability.CalamityCapProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AdrenalineActivate extends C2S {
    public AdrenalineActivate() {
    }

    public AdrenalineActivate(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null)
            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
                adrenaline -> adrenaline.adrenalineActivate(player, true));
    }
}
