package hua223.calamity.net.S2CPacket;

import hua223.calamity.integration.curios.item.StatisNinjaBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class PlayerAutoJump extends S2C {
    private final boolean auto;

    public PlayerAutoJump(boolean auto) {
        this.auto = auto;
    }

    public PlayerAutoJump(FriendlyByteBuf buf) {
        auto = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(auto);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        OptionInstance<Boolean> option = Minecraft.getInstance().options.autoJump();
        if (auto) option.set(true);
        else option.set(StatisNinjaBelt.autoJump);
    }
}
