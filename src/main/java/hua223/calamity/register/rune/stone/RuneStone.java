package hua223.calamity.register.rune.stone;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.rune.ArmorRune;
import org.jetbrains.annotations.NotNull;

public abstract class RuneStone extends ArmorRune {
    public RuneStone() {
        super(RegisterList.UNCOMMON_ONE);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.rune_stone";
    }
}
