package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Decks;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class TaintedDeck extends Decks {

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.player.getHealth() < listener.player.getMaxHealth())
            listener.player.heal(listener.baseAmount * 0.08f);
    }

    @Override
    public Item getUnsealingRope() {
        return CalamityItems.ABYSS_THREAD.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("tainted", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("tainted", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("tainted", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("tainted", 4).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
