package hua223.calamity.net.C2SPacket;

import hua223.calamity.integration.curios.SprintCurio;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ApplySprint extends C2S {
    public ApplySprint() {
    }

    public ApplySprint(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        SprintCurio.onServerResponse(context.getSender());
    }
}