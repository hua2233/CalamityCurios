package hua223.calamity.register.rune.fragments;

import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Echo extends AncientFragments {
    @Override
    protected String prefix() {
        return "echo";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
       IDataPackResponse response = (IDataPackResponse) CalamityItems.METROPOLIS.get();
       response.getPack().putFloat("Bonus", to ? .25f : -.25f);
       response.sendToClient(player);
    }

    @Override
    protected void displayAttributeList(List<Component> tooltips) {
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("echo").withStyle(ChatFormatting.GOLD));
    }
}
