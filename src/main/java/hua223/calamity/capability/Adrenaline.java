package hua223.calamity.capability;

import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Adrenaline implements BaseCap {
    public static final int MAX = 30;
    private static final IDataPackResponse RESPONSE = (IDataPackResponse) CalamityItems.DRAEDON_HEART.get();

    private int value;
    private byte bitFlags;
    private boolean canPlay = true;
    private boolean enabled = false;
    private float amplifier = 1.6f;
    private boolean active = false;
    private float damageOffset = 0.5f;
    private final ServerPlayer player;
    private boolean isNanoMachinesMode = true;

    public Adrenaline(ServerPlayer player) {
        this.player = player;
    }

    public void addValue() {
        if (!enabled || active || isMax()) return;
        CompoundTag pack = RESPONSE.getPack();
        byte bit = 1 << 1;
        pack.putInt("value", ++value);

        if (isMax() && !active && canPlay) {
            canPlay = false;
            bit |= 1 << 3;
        }

        pack.putByte("bit", bit);
        RESPONSE.sendToClient(player);
    }

    public void setEnabled(boolean offOrOn) {
        if (offOrOn != enabled) {
            enabled = offOrOn;
            CompoundTag pack = RESPONSE.getPack();
            byte bit = 1;

            //Make Data Pack And Put DataInfo
            if (enabled) {
                pack.putBoolean("state", true);

                if (value > 0) {
                    bit |= 1 << 1;
                    pack.putInt("value", value);
                }
            } else {
                value = 0;
                canPlay = true;
                active = false;
                pack.putBoolean("state", false);
            }
            pack.putByte("bit", bit);
            RESPONSE.sendToClient(player);
        }
    }

    public boolean isNanoMachinesMode() {
        return isNanoMachinesMode;
    }

    public boolean isActive() {
        return active;
    }

    public void adrenalineActivate(boolean isActive) {
        if (isActive && (!enabled || active || !isMax())) return;

        active = isActive;
        if (isActive) {
            RESPONSE.getPack().putByte("bit", (byte) (1 << 4));
            RESPONSE.sendToClient(player);

            final float heal = isNanoMachinesMode ? player.getMaxHealth() * 0.3f : 0;
            DelayRunnable.conditionsLoop(() -> {
                if (--value <= 0) {
                    active = false;
                    canPlay = true;
                } else if (heal > 0 && value % 10 == 0) player.heal(heal);

                CompoundTag pack = RESPONSE.getPack();
                pack.putInt("value", value);
                pack.putByte("bit", (byte) (1 << 1));
                RESPONSE.sendToClient(player);

                return !active || player.isRemoved();
            }, 2);
        }
    }

    public void zero() {
        if (enabled) {
            value = 0;
            CompoundTag pack = RESPONSE.getPack();
            pack.putInt("value", 0);
            pack.putByte("bit", (byte) (1 << 1));
            RESPONSE.sendToClient(player);
        }
    }

    public float getAmplifier() {
        return amplifier;
    }

    public float getDamageOffset() {
        return damageOffset;
    }

    public boolean isMax() {
        return value >= MAX;
    }

    public void switchMode() {
        if (!enabled) {
            isNanoMachinesMode = !isNanoMachinesMode;
            syncMode(RESPONSE.getPack());
        }
    }

    protected void syncMode(CompoundTag pack) {
        pack.putByte("bit", (byte) (1 << 5));
        pack.putBoolean("nano", isNanoMachinesMode);
        RESPONSE.sendToClient(player);
    }

    public boolean tryUseAdrenalineItem(int flag) {
        if (flag > 7) return false;

        if ((bitFlags & 1 << flag) == 0) {
            bitFlags = (byte) (bitFlags | 1 << flag);
            damageOffset += 0.05f;
            amplifier += 0.2f;

            CompoundTag pack = RESPONSE.getPack();
            pack.putByte("bit", (byte) (1 << 2));
            pack.putByte("count", getAdrenalineItemCount());
            RESPONSE.sendToClient(player);
            return true;
        }

        return false;
    }

    public byte getAdrenalineItemCount() {
        byte count = 0;
        int n = bitFlags;

        while (n != 0) {
            n = n & (n - 1);
            count++;
        }

        return count;
    }

    @SuppressWarnings("ConstantConditions")
    public void onClone(Player old, boolean isDeath) {
        Adrenaline adrenaline = old.Calamity$Player.adrenaline;

        bitFlags = adrenaline.bitFlags;
        amplifier = adrenaline.amplifier;
        value = isDeath ? 0 : adrenaline.value;
        damageOffset = adrenaline.damageOffset;
        isNanoMachinesMode = adrenaline.isNanoMachinesMode;
    }

    public void syncData() {
        CompoundTag tag = RESPONSE.getPack();

        syncMode(tag);
        tag.putByte("bit", (byte) 36);
        tag.putByte("count", getAdrenalineItemCount());

        RESPONSE.sendToClient(player);
    }

    @Override
    public void save(CompoundTag tag) {
        CompoundTag adrenaline = new CompoundTag();

        adrenaline.putInt("value", active ? 0 : value);
        adrenaline.putBoolean("isNano", isNanoMachinesMode);
        adrenaline.putFloat("amplifier", amplifier);
        adrenaline.putFloat("offset", damageOffset);
        adrenaline.putByte("bitFlags", bitFlags);

        tag.put("Adrenaline", adrenaline);
    }

    public void load(CompoundTag tag) {
        CompoundTag adrenaline = tag.getCompound("Adrenaline");

        value = adrenaline.getInt("value");
        isNanoMachinesMode = adrenaline.getBoolean("isNano");
        amplifier = adrenaline.getFloat("amplifier");
        damageOffset = adrenaline.getFloat("offset");
        bitFlags = adrenaline.getByte("bitFlags");
    }
}
