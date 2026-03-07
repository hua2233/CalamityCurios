package hua223.calamity.net.C2SPacket;

import hua223.calamity.register.gui.CalamityCurseMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class OpenEnchantGui extends C2S {
    public OpenEnchantGui() {
    }

    public OpenEnchantGui(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();

        if (player != null) player.openMenu(new CalamityCurseMenuProvider());
    }
}
