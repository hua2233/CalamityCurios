package hua223.calamity.capability;

import hua223.calamity.register.rune.IRuneReinforcement;
import hua223.calamity.register.rune.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class RuneEnchantment {
    private IRuneReinforcement runes;

    public RuneEnchantment() {
    }

    public IRuneReinforcement getRunes() {
        return runes;
    }

    public void setRunes(ItemStack stack, IRuneReinforcement type) {
        if (runes == null && type != null) type.onApply(stack);
        else if (type == null && runes != null) runes.onRemove(stack);

        runes = type;
    }

    public CompoundTag saveNbt() {
        CompoundTag tag = new CompoundTag();
        if (runes != null) runes.saveNbt(tag);
        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        runes = IRuneReinforcement.fromNbt(tag);
    }
}
