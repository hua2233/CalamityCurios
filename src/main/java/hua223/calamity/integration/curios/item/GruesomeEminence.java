package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GruesomeEminence extends BaseCurio {

    public GruesomeEminence(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "GRUESOME_EMINENCE", true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "GRUESOME_EMINENCE", false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 3));
            tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 4));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 1).withStyle(ChatFormatting.DARK_RED));
            tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 2).withStyle(ChatFormatting.DARK_RED));
        }
        return tooltips;
    }
}
