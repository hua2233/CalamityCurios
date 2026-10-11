package hua223.calamity.register.rune.gem;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.rune.ArmorRune;
import org.jetbrains.annotations.NotNull;

public abstract class OriginGem extends ArmorRune {
    public OriginGem() {
        super(RegisterList.EPIC_ONE);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.origin_gem";
    }
}
