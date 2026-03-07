package hua223.calamity.register.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class CalamityCurseMenuProvider implements MenuProvider {
    public CalamityCurseMenuProvider() {
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.calamity_curse");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CalamityCurseMenu(RegisterMenuType.CALAMITY_CURES.get(), id, player);
    }
}
