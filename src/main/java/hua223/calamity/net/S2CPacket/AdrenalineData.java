package hua223.calamity.net.S2CPacket;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.clientInfos.Adrenaline;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AdrenalineData extends S2C {
    private int value = -1;
    private int state = -1;
    private int count = -1;
    private int isNano = -1;
    private int play = -1;

    public AdrenalineData() {
    }

    public AdrenalineData(FriendlyByteBuf buf) {
        value = buf.readInt();
        state = buf.readInt();
        count = buf.readInt();
        isNano = buf.readInt();
        play = buf.readInt();
    }

    public AdrenalineData setValue(int value) {
        this.value = value;
        return this;
    }

    public AdrenalineData setStateForNano(boolean offOrOn) {
        this.isNano = offOrOn ? 1 : 0;
        return this;
    }

    public AdrenalineData setState(boolean offOrOn) {
        this.state = offOrOn ? 1 : 0;
        return this;
    }

    public AdrenalineData setCount(int count) {
        this.count = count;
        return this;
    }

    public AdrenalineData playAnimation(boolean play) {
        this.play = play ? 1 : 0;
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(value);
        byteBuf.writeInt(state);
        byteBuf.writeInt(count);
        byteBuf.writeInt(isNano);
        byteBuf.writeInt(play);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        if (value > -1) Adrenaline.setAdrenalineProgress(value);
        if (state > -1) Adrenaline.setAdrenalineEnabled(state == 1);
        if (count > -1) Adrenaline.setAdrenalineCount(count);
        if (isNano > -1) Adrenaline.setForMachinesMode(isNano == 1);
        if (play > -1) {
            boolean flag = play == 1;
            if (flag) {
                Minecraft.getInstance().player.playSound(CalamitySounds.FULL_ADRENALINE.get());
            }
            Adrenaline.playAnimation(flag);
        }
    }
}
