package hua223.calamity.capability;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public class Adrenaline implements BaseCap<Adrenaline> {
    private static final int MAX = 30;
    private int value;
    private boolean isNanoMachinesMode = true;
    private float amplifier = 1.6f;
    private float damageOffset = 0.5f;
    private boolean active = false;
    private boolean enabled = false;
    private boolean canPlay = true;
    private byte bitFlags;

    public void addValue(ServerPlayer player, IDataPackResponse response) {
        if (!enabled || active || isMax()) return;
        CompoundTag pack = response.getPack();
        pack.putInt("value", ++value);

        if (isMax() && !active && canPlay) {
            canPlay = false;
            pack.putBoolean("play", true);
        }

        response.sendToClient(player);
    }

    public void setEnabled(ServerPlayer player, boolean offOrOn, IDataPackResponse response) {
        if (offOrOn != enabled) {
            enabled = offOrOn;
            if (enabled) {
                //Make Data Pack And Put DataInfo
                CompoundTag flags = response.getPack();
                flags.putInt("value", value);
                flags.putBoolean("state", true);
                flags.putByte("count", getAdrenalineItemCount());
                flags.putBoolean("isNano", isNanoMachinesMode);
                response.sendToClient(player);
            } else {
                canPlay = true;
                value = 0;
                active = false;
                response.getPack().putBoolean("state", false);
                response.sendToClient(player);
            }
        }
    }

    public boolean isNanoMachinesMode() {
        return isNanoMachinesMode;
    }

    public boolean isActive() {
        return active;
    }

    public void adrenalineActivate(ServerPlayer player, boolean isActive, IDataPackResponse response) {
        if (isActive && (!enabled || active || !isMax())) return;
        active = isActive;
        if (isActive) {
            if (isNanoMachinesMode) {
                player.level().playSound(null, player, CalamitySounds.NANO_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
                startNanoRepair(player, response);
            } else {
                startAdrenalineMode(player, response);
                player.level().playSound(null, player, CalamitySounds.ADRENALINE_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }

    public void zero(ServerPlayer player, IDataPackResponse response) {
        if (enabled) {
            value = 0;
            response.getPack().putInt("value", 0);
            response.sendToClient(player);
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

    public void switchMode(ServerPlayer player, IDataPackResponse response) {
        isNanoMachinesMode = !isNanoMachinesMode;
        value = 0;
        canPlay = true;
        //一般卸下时调用
        CompoundTag tag = response.getPack();
        tag.putInt("value", value);
        tag.putBoolean("isNano", isNanoMachinesMode);
        response.sendToClient(player);
    }

    private void startNanoRepair(ServerPlayer player, IDataPackResponse response) {
        final float heal = player.getMaxHealth() * 0.3f;

        DelayRunnable.conditionsLoop(() -> {
            if (value <= 0) {
                active = false;
                canPlay = true;
                if (isNanoMachinesMode) {
                    response.getPack().putBoolean("play", false);
                    response.sendToClient(player);
                }
                return true;
            } else {
                if (value % 10 == 0) player.heal(heal);
                response.getPack().putInt("value", --value);
                response.sendToClient(player);
                return false;
            }
        }, 2);
    }

    private void startAdrenalineMode(ServerPlayer player, IDataPackResponse response) {
        DelayRunnable.conditionsLoop(() -> {
            if (value <= 0) {
                active = false;
                canPlay = true;
                return true;
            } else {
                response.getPack().putInt("value", --value);
                response.sendToClient(player);
                return false;
            }
        }, 2);
    }

    public boolean tryUseAdrenalineItem(int flag, ServerPlayer player) {
        if (flag > 7) return false;

        if ((bitFlags & 1 << flag) == 0) {
            bitFlags = (byte) (bitFlags | 1 << flag);
            damageOffset += 0.05f;
            amplifier += 0.2f;
            IDataPackResponse response = (IDataPackResponse) CalamityItems.DRAEDON_HEART.get();
            response.getPack().putByte("count", getAdrenalineItemCount());
            response.sendToClient(player);
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

    public void deathActivation(Adrenaline adrenaline, ServerPlayer player) {
        isNanoMachinesMode = adrenaline.isNanoMachinesMode;
        amplifier = adrenaline.amplifier;
        damageOffset = adrenaline.damageOffset;
        bitFlags = adrenaline.bitFlags;
        syncData(player);
    }

    public void syncData(ServerPlayer player) {
        IDataPackResponse response = (IDataPackResponse) CalamityItems.DRAEDON_HEART.get();
        CompoundTag tag = response.getPack();
        tag.putInt("value", value);
        tag.putBoolean("state", enabled);
        tag.putByte("count", getAdrenalineItemCount());
        tag.putBoolean("isNano", isNanoMachinesMode);
        if (isNanoMachinesMode && isMax()) tag.putBoolean("play", true);

        response.sendToClient(player);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("value", active ? 0 : value);
        tag.putBoolean("isNano", isNanoMachinesMode);
        tag.putFloat("amplifier", amplifier);
        tag.putFloat("offset", damageOffset);
        tag.putByte("bitFlags", bitFlags);
    }

    public void load(CompoundTag tag) {
        value = tag.getInt("value");
        isNanoMachinesMode = tag.getBoolean("isNano");
        amplifier = tag.getFloat("amplifier");
        damageOffset = tag.getFloat("offset");
        bitFlags = tag.getByte("bitFlags");
    }
}
