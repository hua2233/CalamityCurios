package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Tarnish extends Card {
    public Tarnish(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        boolean flag = CalamityHelp.getCalamityFlag(listener.player, 10);
        listener.entity.setSecondsOnFire(flag ? 8 : 4);
        listener.amplifier += flag ? 0.12f : 0.08f;
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("tarnish").withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
