package hua223.calamity.capability;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class Rage implements BaseCap<Rage> {
    private static final float MAX_VALUE = 100;
    private float rageValue;
    private int extraTick = 0;
    private int mutableTick = 0;
    private boolean enabled = false;
    private boolean active = false;
    private boolean canPlay = true;
    private byte shatteredLevel = 0;
    private boolean attenuation = true;
    private int currentDamage = 0;
    private int levelUpDamage = 300;
    private byte bitFlags;

    public void addValue(float value, ServerPlayer player, IDataPackResponse response) {
        if (!enabled || rageValue >= MAX_VALUE) return;
        if (active && attenuation) value /= 2;
        rageValue = Math.min(MAX_VALUE, rageValue + value);
        response.getPack().putFloat("value", rageValue);
        response.sendToClient(player);
        playAnimation(player, response);
    }

    public float getValue() {
        return rageValue;
    }

    public void activeRage(ServerPlayer player, IDataPackResponse response) {
        if (!enabled || active || MAX_VALUE != rageValue) return;
        active = true;
        player.level().playSound(null, player, CalamitySounds.RAGE_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
        activeState(player, response);
    }

    public boolean tryUseRageItem(int flag, ServerPlayer player) {
        if (flag > 7) return false;

        if ((bitFlags & 1 << flag) == 0) {
            bitFlags = (byte) (bitFlags | 1 << flag);
            extraTick += 20;
            mutableTick += 20;
            IDataPackResponse response = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
            response.getPack().putByte("count", getRageItemCount());
            response.sendToClient(player);
            return true;
        }

        return false;
    }

    public byte getRageItemCount() {
        byte count = 0;
        int n = bitFlags;

        while (n != 0) {
            n = n & (n - 1);
            count++;
        }

        return count;
    }

    public void activeState(ServerPlayer player, IDataPackResponse response) {
        if (active) {
            DelayRunnable.conditionsLoop(() -> {
                if (--mutableTick <= 0 && --rageValue <= 0) {
                    rageValue = 0;
                    canPlay = true;
                    mutableTick = extraTick;
                    active = false;
                    return true;
                    //CalamitySounds.playSound(CalamitySounds.RAGE_END, player);
                }

                response.getPack().putFloat("value", rageValue);
                response.sendToClient(player);
                return false;
            }, 2);
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean offOrOn, ServerPlayer player, IDataPackResponse response) {
        if (enabled != offOrOn) {
            CompoundTag pack = response.getPack();
            enabled = offOrOn;
            if (!enabled) {
                active = false;
                rageValue = 0;
                pack.putFloat("value", rageValue);
                canPlay = true;
            }
            pack.putBoolean("state", enabled);
            response.sendToClient(player);
        }
    }

    public void playAnimation(ServerPlayer player, IDataPackResponse response) {
        if (MAX_VALUE == rageValue && !active && canPlay) {
            canPlay = false;
            response.getPack().putByte("play", (byte) 0);
            response.sendToClient(player);
        }
    }

    public void setAttenuation(boolean canAttenuation) {
        attenuation = canAttenuation;
    }

    public void syncData(ServerPlayer player) {
        IDataPackResponse response = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
        CompoundTag pack = response.getPack();
        pack.putFloat("value", rageValue);
        pack.putBoolean("state", enabled);
        pack.putInt("damage", currentDamage);
        pack.putInt("upDamage", levelUpDamage);
        pack.putByte("level", shatteredLevel);
        pack.putByte("count", getRageItemCount());
        response.sendToClient(player);
    }

    public void addLevelUpProgress(int value, ServerPlayer player, IDataPackResponse response) {
        //if (!active) return;
        currentDamage += value;
        CompoundTag pack = response.getPack();
        if (currentDamage >= levelUpDamage && shatteredLevel < 127) {
            currentDamage = 0;
            levelUpDamage += 300 * (++shatteredLevel);
            pack.putByte("level", shatteredLevel);
            pack.putInt("upDamage", levelUpDamage);
        } else pack.putInt("damage", currentDamage);
        response.sendToClient(player);
    }

    public void deathActivation(Rage rage, ServerPlayer player) {
        shatteredLevel = rage.shatteredLevel;
        currentDamage = rage.currentDamage;
        levelUpDamage = rage.levelUpDamage;
        bitFlags = rage.bitFlags;
        IDataPackResponse response = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
        CompoundTag pack = response.getPack();
        pack.putBoolean("state", false);
        pack.putFloat("value", rageValue);
        response.sendToClient(player);
    }

    public float getLevelBonus() {
        return 0.35f + shatteredLevel * 0.02f;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putFloat("value", active ? 0 : rageValue);
        tag.putBoolean("enabled", enabled);
        tag.putInt("level", shatteredLevel);
        tag.putInt("current", currentDamage);
        tag.putInt("levelUp", levelUpDamage);
        tag.putInt("extra", extraTick);
        tag.putByte("flags", bitFlags);
    }

    @Override
    public void load(CompoundTag tag) {
        rageValue = tag.getFloat("value");
        enabled = tag.getBoolean("enabled");
        shatteredLevel = tag.getByte("level");
        currentDamage = tag.getInt("current");
        levelUpDamage = tag.getInt("levelUp");
        extraTick = tag.getInt("extra");
        mutableTick = extraTick;
        bitFlags = tag.getByte("flags");
    }
}
