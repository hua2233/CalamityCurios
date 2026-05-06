package hua223.calamity.register.gui;

import hua223.calamity.register.RegisterList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CalamityCurseMenuProvider implements MenuProvider {
    public CalamityCurseMenuProvider() {
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("menu.calamity_curse");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new CalamityCurseMenu(RegisterList.CALAMITY_CURES.get(), id, player);
    }
}
