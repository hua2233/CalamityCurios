package hua223.calamity.net.C2SPacket;

import hua223.calamity.integration.curios.Wings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class FlyInfinite extends C2S {
    public FlyInfinite() {

    }

    public FlyInfinite(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        Wings.setFlyInfinite(context.getSender());
    }
}
