package hua223.calamity.register.rune.fragments;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Soul extends AncientFragments {
    @Override
    protected String prefix() {
        return "soul";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        removeOrApplyEventsBasedOnData(0, .02f, to, player);
    }

    @ApplyEvent(180)
    public final void onAttack(PostAttackListener listener) {
        CalamityPlayer player = listener.player.Calamity$Player;
        float[] proportion = player.getRuneData(this);
        float heal = listener.getCorrectionValue() * proportion[0];
        if (heal > .1f) listener.player.heal(heal);
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    protected void displayAttributeList(List<Component> tooltips) {
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable(prefix()).withStyle(ChatFormatting.GOLD));
    }
}
