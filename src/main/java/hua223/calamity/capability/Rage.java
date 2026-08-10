package hua223.calamity.capability;

import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Rage implements BaseCap {
    private static final IDataPackResponse RESPONSE = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
    private static final float MAX_VALUE = 100;
    private final ServerPlayer player;
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

    public Rage(ServerPlayer player) {
        this.player = player;
    }

    public void addValue(float value) {
        if (!enabled || rageValue >= MAX_VALUE) return;
        if (active && attenuation) value /= 2;
        rageValue += value;

        CompoundTag tag = RESPONSE.getPack();
        float v = rageValue;
        if (MAX_VALUE <= rageValue) {
            rageValue = MAX_VALUE;
            if (!active && canPlay) {
                canPlay = false;
                v = -v;
            }
        }

        tag.putFloat("value", v);
        RESPONSE.sendToClient(player);
    }

    public float getValue() {
        return rageValue;
    }

    public void activeRage() {
        if (!enabled || active || MAX_VALUE != rageValue) return;
        active = true;
        CalamitySounds.RAGE_ACTIVATE.playSound(player);
        activeState();
    }

    public boolean tryUseRageItem(int flag) {
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

    public void activeState() {
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

                RESPONSE.getPack().putFloat("value", rageValue);
                RESPONSE.sendToClient(player);
                return false;
            }, 2);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setEnabled(boolean offOrOn) {
        if (enabled != offOrOn) {
            CompoundTag pack = RESPONSE.getPack();
            enabled = offOrOn;
            if (!enabled) {
                active = false;
                rageValue = 0;
                pack.putFloat("value", rageValue);
                canPlay = true;
            }
            pack.putBoolean("state", enabled);
            RESPONSE.sendToClient(player);
        }
    }

    public void setAttenuation(boolean canAttenuation) {
        attenuation = canAttenuation;
    }

    public void syncData() {
        CompoundTag pack = RESPONSE.getPack();
        pack.putFloat("value", rageValue == MAX_VALUE ? -rageValue : rageValue);
        pack.putBoolean("state", enabled);
        pack.putInt("damage", currentDamage);
        pack.putInt("upDamage", levelUpDamage);
        pack.putByte("level", shatteredLevel);
        pack.putByte("count", getRageItemCount());
        RESPONSE.sendToClient(player);
    }

    public void addLevelUpProgress(int value) {
        //if (!active) return;
        currentDamage += value;
        CompoundTag pack = RESPONSE.getPack();
        if (currentDamage >= levelUpDamage && shatteredLevel < 127) {
            currentDamage = 0;
            levelUpDamage += 300 * (++shatteredLevel);
            pack.putByte("level", shatteredLevel);
            pack.putInt("upDamage", levelUpDamage);
        } else pack.putInt("damage", currentDamage);
        RESPONSE.sendToClient(player);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClone(Player old, boolean isDeath) {
        Rage rage = old.Calamity$Player.rage;
        shatteredLevel = rage.shatteredLevel;
        currentDamage = rage.currentDamage;
        levelUpDamage = rage.levelUpDamage;
        bitFlags = rage.bitFlags;
        IDataPackResponse response = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
        CompoundTag pack = response.getPack();
        pack.putBoolean("state", enabled);
        rageValue = isDeath ? 0 : rage.rageValue;
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
