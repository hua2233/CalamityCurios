package hua223.calamity.net.S2CPacket;

import hua223.calamity.util.RenderUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class OmnisciencePerception extends S2C {
    public OmnisciencePerception() {}

    public OmnisciencePerception(FriendlyByteBuf buf) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        RenderUtil.Shaders.renderHighlightBlocks(false);
    }
}
