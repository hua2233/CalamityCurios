package hua223.calamity.net.S2CPacket;

import hua223.calamity.capability.CalamityCap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class CalamitySync extends S2C {
    private final String[] type;

    public CalamitySync(CalamityCap.CurseType... type) {
        this.type = new String[type.length];
        for (int i = 0; i < type.length; i++)
            this.type[i] = type[i].name();
    }

    public CalamitySync(FriendlyByteBuf buf) {
        byte size = buf.readByte();
        type = new String[size];

        for (int i = 0; i < size; i++)
            type[i] = buf.readUtf();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        CalamityCap.setText(type);
    }
}
