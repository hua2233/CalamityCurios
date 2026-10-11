package hua223.calamity.register.rune.scroll;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.rune.ArmorRune;
import org.jetbrains.annotations.NotNull;

public abstract class EnchantedScroll extends ArmorRune {
    public EnchantedScroll() {
        super(RegisterList.RARE_ONE);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.enchanted_scroll";
    }
}
