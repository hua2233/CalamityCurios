package hua223.calamity.net.C2SPacket;

import hua223.calamity.register.gui.CalamityCurseMenu;
import hua223.calamity.register.gui.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class SpellTypeSync extends C2S {
    private final String type;

    public SpellTypeSync(String type) {
        this.type = type;
    }

    public SpellTypeSync(FriendlyByteBuf buf) {
        type = buf.readUtf();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(type);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof CalamityCurseMenu menu) {
            menu.type = SpellType.valueOf(type);
        }
    }
}
