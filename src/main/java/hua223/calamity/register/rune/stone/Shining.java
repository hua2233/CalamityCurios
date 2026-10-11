package hua223.calamity.register.rune.stone;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Shining extends RuneStone {
    @Override
    protected String prefix() {
        return "shining";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        applyArmorLight(to ? 2 : -2, player);
    }

    @Override
    public int[] addData() {
        return new int[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayLightValue(tooltips, 2);
    }
}
