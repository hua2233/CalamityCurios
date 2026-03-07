package hua223.calamity.net.S2CPacket;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.clientInfos.Rage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class RageDataSync extends S2C {
    private float value = -1;
    private int start = -1;
    private int damage = -1;
    private int maxDamage = -1;
    private int level = -1;
    private int count = -1;
    private boolean play = false;

    public RageDataSync() {
    }

    public RageDataSync(FriendlyByteBuf buf) {
        value = buf.readFloat();
        start = buf.readInt();
        damage = buf.readInt();
        maxDamage = buf.readInt();
        level = buf.readInt();
        count = buf.readInt();
        play = buf.readBoolean();
    }


    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(value);
        byteBuf.writeInt(start);
        byteBuf.writeInt(damage);
        byteBuf.writeInt(maxDamage);
        byteBuf.writeInt(level);
        byteBuf.writeInt(count);
        byteBuf.writeBoolean(play);
    }

    public RageDataSync setLevel(int level) {
        this.level = level;
        return this;
    }

    public RageDataSync setValue(float value) {
        this.value = value;
        return this;
    }

    public RageDataSync setMaxProgress(int maxDamage) {
        this.maxDamage = maxDamage;
        return this;
    }

    public RageDataSync setProgress(int damage) {
        this.damage = damage;
        return this;
    }

    public RageDataSync start(boolean offOrOn) {
        start = offOrOn ? 1 : 0;
        return this;
    }

    public RageDataSync setCount(int count) {
        this.count = count;
        return this;
    }

    public RageDataSync playAnimation() {
        this.play = true;
        return this;
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        if (value > -1) Rage.setRageProgress(value);
        if (start > -1) Rage.rageEnabled = start == 1;
        if (damage > -1) Rage.setCurrentDamage(damage);
        if (maxDamage > -1) Rage.setLevelUpDamage(maxDamage);
        if (level > -1) Rage.setLevel(level);
        if (count > -1) Rage.setRageCount(count);
        if (play) {
            Minecraft.getInstance().player.playSound(CalamitySounds.FULL_RAGE.get());
            Rage.animationFrameTime = true;
        }
    }
}
