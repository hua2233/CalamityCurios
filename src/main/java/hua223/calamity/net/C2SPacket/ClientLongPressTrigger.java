package hua223.calamity.net.C2SPacket;

import hua223.calamity.util.ILongPressAvailable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class ClientLongPressTrigger extends C2S {
    public ClientLongPressTrigger() {}

    public ClientLongPressTrigger(FriendlyByteBuf buf) {}

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ILongPressAvailable available)
                available.onServerResponse(player, stack);
        }
    }
}
