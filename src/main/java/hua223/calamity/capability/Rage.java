package hua223.calamity.capability;

import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Rage implements BaseCap {
    private static final IDataPackResponse RESPONSE = (IDataPackResponse) CalamityItems.SHATTERED_COMMUNITY.get();
    public static final float MAX_VALUE = 100;
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

        float v = rageValue;
        if (MAX_VALUE <= rageValue) {
            rageValue = MAX_VALUE;
            if (!active && canPlay) {
                canPlay = false;
                v = -v;
            }
        }

        syncValue(RESPONSE.getPack(), v);
        RESPONSE.sendToClient(player);
    }

    public float getValue() {
        return rageValue;
    }

    public void activeRage() {
        if (!enabled || active || MAX_VALUE != rageValue) return;
        active = true;
        RESPONSE.getPack().putByte("bit", (byte) (1 << 5));
        RESPONSE.sendToClient(player);
        activeState();
    }

    private void syncValue(CompoundTag pack, float rageValue) {
        pack.putFloat("value", rageValue);
        pack.putByte("bit", (byte) (1 << 2));
    }

    public boolean tryUseRageItem(int flag) {
        if (flag > 7) return false;

        if ((bitFlags & 1 << flag) == 0) {
            bitFlags = (byte) (bitFlags | 1 << flag);
            extraTick += 20;
            mutableTick += 20;
            CompoundTag pack = RESPONSE.getPack();

            pack.putByte("bit", (byte) (1 << 1));
            pack.putByte("count", getRageItemCount());
            RESPONSE.sendToClient(player);
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

    private void activeState() {
        if (active) {
            DelayRunnable.conditionsLoop(() -> {
                if (player.isRemoved()) return true;

                boolean result = false;
                if (--mutableTick <= 0 && --rageValue <= 0) {
                    rageValue = 0;
                    canPlay = true;
                    mutableTick = extraTick;
                    active = false;
                    result = true;
                }

                syncValue(RESPONSE.getPack(), rageValue);
                RESPONSE.sendToClient(player);
                return result;
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
            byte bit = 1;
            if (!enabled) {
                bit = 1 | 1 << 2;
                rageValue = 0;
                active = false;
                canPlay = true;
                pack.putFloat("value", rageValue);
            }

            pack.putBoolean("state", enabled);
            pack.putByte("bit", (byte) (bit | 1));
            RESPONSE.sendToClient(player);
        }
    }

    public void setAttenuation(boolean canAttenuation) {
        attenuation = canAttenuation;
    }

    public void syncData() {
        CompoundTag pack = RESPONSE.getPack();
        pack.putByte("bit", (byte) 0x1F);
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
        byte bit = 1 << 3;
        if (currentDamage >= levelUpDamage && shatteredLevel < 127) {
            bit = 1 << 4;
            currentDamage = 0;
            levelUpDamage += 300 * (++shatteredLevel);
            pack.putByte("level", shatteredLevel);
            pack.putInt("upDamage", levelUpDamage);
        } else pack.putInt("damage", currentDamage);

        pack.putByte("bit", bit);
        RESPONSE.sendToClient(player);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClone(Player old, boolean isDeath) {
        Rage rage = old.Calamity$Player.rage;

        bitFlags = rage.bitFlags;
        currentDamage = rage.currentDamage;
        levelUpDamage = rage.levelUpDamage;
        shatteredLevel = rage.shatteredLevel;
        rageValue = isDeath ? 0 : rage.rageValue;

        CompoundTag pack = RESPONSE.getPack();
        pack.putBoolean("state", enabled);
        pack.putFloat("value", rageValue);
        pack.putByte("bit", (byte) (1 | 1 << 2));
        RESPONSE.sendToClient(player);
    }

    public float getLevelBonus() {
        return 0.35f + shatteredLevel * 0.02f;
    }

    @Override
    public void save(CompoundTag tag) {
        CompoundTag rage = new CompoundTag();

        rage.putFloat("value", active ? 0 : rageValue);
        rage.putBoolean("enabled", enabled);
        rage.putInt("level", shatteredLevel);
        rage.putInt("current", currentDamage);
        rage.putInt("levelUp", levelUpDamage);
        rage.putInt("extra", extraTick);
        rage.putByte("flags", bitFlags);

        tag.put("Rage", rage);
    }

    @Override
    public void load(CompoundTag tag) {
        CompoundTag rage = tag.getCompound("Rage");

        rageValue = rage.getFloat("value");
        enabled = rage.getBoolean("enabled");
        shatteredLevel = rage.getByte("level");
        currentDamage = rage.getInt("current");
        levelUpDamage = rage.getInt("levelUp");
        extraTick = rage.getInt("extra");
        mutableTick = extraTick;
        bitFlags = rage.getByte("flags");
    }
}
