package hua223.calamity.capability;

import hua223.calamity.register.gui.SpellType;
import net.minecraft.nbt.CompoundTag;

public class CurseEnchantment {
    private SpellType runes;

    private boolean dirty = true;
    private CompoundTag lastTag;

    public CurseEnchantment() {
    }

    public boolean isEffective() {
        return runes != null;
    }

    public SpellType getRunes() {
        return runes;
    }

    public void setRunes(SpellType type) {
        runes = type;
        dirty = true;
    }

    public CompoundTag saveNbt() {
        if (dirty) {
            CompoundTag tag = new CompoundTag();
            tag.putString("SpellType", runes == null ? "" : runes.name());
            dirty = false;
            lastTag = tag;
        }

        return lastTag;
    }

    public void loadNbt(CompoundTag tag) {
        String s = tag.getString("SpellType");
        if (!s.isEmpty()) this.setRunes(SpellType.valueOf(s));
    }
}
