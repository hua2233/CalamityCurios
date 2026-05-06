package hua223.calamity.capability;

import hua223.calamity.register.gui.SpellType;
import net.minecraft.nbt.CompoundTag;

public class CurseEnchantment {
    private SpellType runes;

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
    }

    public CompoundTag saveNbt() {
        CompoundTag tag = new CompoundTag();
        if (runes != null) tag.putString("SpellType", runes.name());
        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        String s = tag.getString("SpellType");
        if (!s.isEmpty()) setRunes(SpellType.valueOf(s));
    }
}
