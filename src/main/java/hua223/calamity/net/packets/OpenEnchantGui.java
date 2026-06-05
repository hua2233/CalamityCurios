package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.gui.CalamityCurseMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class OpenEnchantGui extends DataPack {
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
