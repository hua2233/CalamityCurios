package hua223.calamity.capability;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.RageDataSync;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class Rage implements BaseCap<Rage> {
    private static final float maxValue = 100;
    private float rageValue;
    private int extraTick = 0;
    private int mutableTick = 0;
    private boolean enabled = false;
    private boolean active = false;
    private boolean canPlay = true;
    private int shatteredLevel = 0;
    private boolean attenuation = true;
    private int currentDamage = 0;
    private int levelUpDamage = 300;
    private boolean[] tags = new boolean[3];

    public void addValue(float value, ServerPlayer player) {
        if (!enabled || rageValue >= maxValue) return;
        if (active && attenuation) value /= 2;
        rageValue = Math.min(maxValue, rageValue + value);
        NetMessages.sendToClient(new RageDataSync().setValue(rageValue), player);
        playAnimation(player);
    }

    public void setRageValue(float value, ServerPlayer player) {
        if (enabled) {
            rageValue = Mth.clamp(value, 0, maxValue);
            NetMessages.sendToClient(new RageDataSync().setValue(rageValue), player);
            if (value == 0) canPlay = true;
        }
    }

    public float getValue() {
        return rageValue;
    }

    public void activeRage(boolean isActive, ServerPlayer player) {
        if (isActive && (!enabled || active || maxValue != rageValue)) return;
        active = isActive;
        player.level.playSound(null, player, CalamitySounds.RAGE_ACTIVATE.get(), SoundSource.PLAYERS, 1f, 1f);
        activeState(player);
    }

    public boolean tryUseRageItem(int tick, int tag, ServerPlayer player) {
        if (tag > 2) return false;

        if (!tags[tag]) {
            tags[tag] = true;
            extraTick += tick;
            mutableTick += tick;
            NetMessages.sendToClient(new RageDataSync().setCount(getRageItemCount()), player);
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

    public void activeState(ServerPlayer player) {
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

                NetMessages.sendToClient(new RageDataSync().setValue(rageValue), player);
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

    public void setEnabled(boolean offOrOn, ServerPlayer player) {
        if (enabled != offOrOn) {
            enabled = offOrOn;
            if (!enabled) active = false;
            NetMessages.sendToClient(new RageDataSync().start(offOrOn), player);
        }
    }

    public void playAnimation(ServerPlayer player) {
        if (maxValue == rageValue && !active && canPlay) {
            canPlay = false;
            NetMessages.sendToClient(new RageDataSync().playAnimation(), player);
        }
    }

    public void setAttenuation(boolean canAttenuation) {
        attenuation = canAttenuation;
    }

    public void syncData(ServerPlayer player) {
        NetMessages.sendToClient(new RageDataSync()
            .start(enabled).setValue(rageValue).setProgress(currentDamage)
            .setMaxProgress(levelUpDamage).setLevel(shatteredLevel).setCount(getRageItemCount()), player);
    }

    public void addLevelUpProgress(int value, ServerPlayer player) {
        //if (!active) return;
        currentDamage += value;
        if (currentDamage >= levelUpDamage) {
            currentDamage = 0;
            levelUpDamage += 300 * (++shatteredLevel);
            NetMessages.sendToClient(new RageDataSync()
                .setMaxProgress(levelUpDamage).setLevel(shatteredLevel), player);
        }
        NetMessages.sendToClient(new RageDataSync().setProgress(currentDamage), player);
    }

    public void deathActivation(Rage rage, ServerPlayer player) {
        shatteredLevel = rage.shatteredLevel;
        currentDamage = rage.currentDamage;
        levelUpDamage = rage.levelUpDamage;
        tags = rage.tags;
        NetMessages.sendToClient(new RageDataSync().setValue(rageValue), player);
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
        tag.putInt("level_up", levelUpDamage);
        tag.putInt("extra", extraTick);
        for (int i = 0; i < 3; i++) {
            tag.putBoolean("tag" + i, tags[i]);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        rageValue = tag.getFloat("value");
        enabled = tag.getBoolean("enabled");
//        canPlay = tag.getBoolean("can_play");
        shatteredLevel = tag.getInt("level");
        currentDamage = tag.getInt("current");
        levelUpDamage = tag.getInt("level_up");
        extraTick = tag.getInt("extra");
        mutableTick = extraTick;
        for (int i = 0; i < 3; i++) {
            tags[i] = tag.getBoolean("tag" + i);
        }
    }
}
