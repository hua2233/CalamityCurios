package hua223.calamity.net.packets;

import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class ApplySprint extends DataPack {
    public ApplySprint() {
    }

    public ApplySprint(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        SprintCurio.onServerResponse(context.getSender());
    }
}