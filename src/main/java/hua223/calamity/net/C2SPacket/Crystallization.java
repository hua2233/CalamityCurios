package hua223.calamity.net.C2SPacket;

import hua223.calamity.integration.curios.item.BlazingCore;
import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class Crystallization extends C2S {
    public Crystallization() {
    }

    public Crystallization(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        ((BlazingCore) CalamityItems.BLAZING_CORE.get()).active(player);
    }
}
