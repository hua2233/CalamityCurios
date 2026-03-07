package hua223.calamity.integration.curios;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Optional;

public abstract class Card extends BaseCurio {
    public Card(Properties properties) {
        super(properties);
        //When the server starts the Tick event, mount it to the main deck
        //Called after all items are registered, we cannot determine whether the card set is registered before all cards
        DelayRunnable.nextTickRun(() -> {
            if (getAffiliatedWith() instanceof Decks deck)
                deck.getCards().add(this);
            else CalamityCurios.LOGGER.error("The card cannot be registered to a non card group item!!! type: {}", this);
        });
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Optional<IItemHandlerModifiable> optional = CuriosApi.getCuriosHelper().getEquippedCurios(slotContext.entity()).resolve();
        if (optional.isPresent()) {
            IItemHandlerModifiable modifiable = optional.get();
            Item deck = getAffiliatedWith();
            for (int i = 0; i < modifiable.getSlots(); i++) {
                ItemStack curio = modifiable.getStackInSlot(i);
                if (curio.is(this) || curio.is(deck)) return false;
            }

            return true;
        }
        return false;
    }

    public static boolean equipFromDeck(ItemStack stack) {
        return stack.getItem() instanceof Decks;
    }

    protected abstract Item getAffiliatedWith();
}
