package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Decks;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.world.item.Item;

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
}
