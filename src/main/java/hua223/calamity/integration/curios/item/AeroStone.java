package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class AeroStone extends BaseCurio {
    public AeroStone(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        Wings.extraFlyTime += 80;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        Wings.extraFlyTime -= 80;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) tooltips.add(CMLangUtil.getTranslatable("aero_stone", 2));
        else tooltips.add(CMLangUtil.getTranslatable("aero_stone", 1));
        return tooltips;
    }
}
