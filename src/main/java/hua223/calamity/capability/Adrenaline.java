package hua223.calamity.capability;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.AdrenalineData;
import hua223.calamity.register.sounds.CalamitySounds;
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
    private boolean[] tags = new boolean[3];

    public void addValue(ServerPlayer player) {
        if (!enabled || active || isMax()) return;
        NetMessages.sendToClient(new AdrenalineData().setValue(++value), player);
        playAnimation(player);
    }

    public void setEnabled(ServerPlayer player, boolean offOrOn) {
        if (offOrOn != enabled) {
            enabled = offOrOn;
            AdrenalineData data = new AdrenalineData().setState(offOrOn);

            if (!enabled) {
                canPlay = true;
                value = 0;
                active = false;
            } else data.setStateForNano(isNanoMachinesMode);
            NetMessages.sendToClient(data, player);
        }
    }

    public boolean isNanoMachinesMode() {
        return isNanoMachinesMode;
    }

    public boolean isActive() {
        return active;
    }

    public void adrenalineActivate(ServerPlayer player, boolean isActive) {
        if (isActive && (!enabled || active || !isMax())) return;
        active = isActive;
        if (isActive) {
            if (isNanoMachinesMode) {
                startNanoRepair(player);
                player.level.playSound(null, player, CalamitySounds.NANO_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
            } else {
                startAdrenalineMode(player);
                player.level.playSound(null, player, CalamitySounds.ADRENALINE_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }

    public void zero(ServerPlayer player) {
        if (enabled) {
            value = 0;
            NetMessages.sendToClient(new AdrenalineData().setValue(0), player);
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

    public void playAnimation(ServerPlayer player) {
        if (isMax() && !active && canPlay) {
            canPlay = false;
            NetMessages.sendToClient(new AdrenalineData().playAnimation(true), player);
        }
    }

    public void switchMode(ServerPlayer player) {
        isNanoMachinesMode = !isNanoMachinesMode;
        value = 0;
        canPlay = true;
        NetMessages.sendToClient(new AdrenalineData()
            .setValue(value).setStateForNano(isNanoMachinesMode), player);
    }

    private void startNanoRepair(ServerPlayer player) {
        final float heal = player.getMaxHealth() * 0.3f;
        DelayRunnable.conditionsLoop(() -> {
            if (value <= 0) {
                active = false;
                canPlay = true;
                if (isNanoMachinesMode) NetMessages.sendToClient(new AdrenalineData().playAnimation(false), player);
                return true;
            } else {
                if (value % 10 == 0) player.heal(heal);
                NetMessages.sendToClient(new AdrenalineData().setValue(--value), player);
                return false;
            }
        }, 2);
    }

    private void startAdrenalineMode(ServerPlayer player) {
        DelayRunnable.conditionsLoop(() -> {
            if (value <= 0) {
                active = false;
                canPlay = true;
                return true;
            } else {
                NetMessages.sendToClient(new AdrenalineData().setValue(--value), player);
                return false;
            }
        }, 2);
    }

    public boolean tryUseAdrenalineItem(int tag, ServerPlayer player) {
        if (tag > 2) return false;

        if (!tags[tag]) {
            tags[tag] = true;
            damageOffset += 0.05f;
            amplifier += 0.2f;
            NetMessages.sendToClient(new AdrenalineData().setCount(getRageItemCount()), player);
            return true;
        }

        return false;
    }

    public int getRageItemCount() {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if (tags[i]) count++;
        }

        return count;
    }

    public void deathActivation(Adrenaline adrenaline, ServerPlayer player) {
        isNanoMachinesMode = adrenaline.isNanoMachinesMode;
        amplifier = adrenaline.amplifier;
        damageOffset = adrenaline.damageOffset;
        tags = adrenaline.tags;
        syncData(player);
    }

    public void syncData(ServerPlayer player) {
        AdrenalineData data = new AdrenalineData().setValue(value)
            .setState(enabled).setCount(getRageItemCount()).setStateForNano(isNanoMachinesMode);
        if (isNanoMachinesMode && isMax()) data.playAnimation(true);
        NetMessages.sendToClient(data, player);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("value", active ? 0 : value);
        tag.putBoolean("nano", isNanoMachinesMode);
        tag.putFloat("amplifier", amplifier);
        tag.putFloat("offset", damageOffset);
        tag.putBoolean("enabled", enabled);
        for (int i = 0; i < 3; i++) {
            tag.putBoolean("tag" + i, tags[i]);
        }
    }

    public void load(CompoundTag tag) {
        value = tag.getInt("value");
        isNanoMachinesMode = tag.getBoolean("nano");
        amplifier = tag.getFloat("amplifier");
        damageOffset = tag.getFloat("offset");
        enabled = tag.getBoolean("enabled");
        for (int i = 0; i < 3; i++) {
            tags[i] = tag.getBoolean("tag" + i);
        }
    }
}
