package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ShadowSpecBar extends TooltipItem implements IBackwardsCompatDefaultNbtItem  {
    public ShadowSpecBar() {
        super(new Item.Properties().stacksTo(64).fireResistant()
            .rarity(RegisterList.CALAMITY_RARITY), "shadow_spec_bar");
    }

    @Override
    public void setupItem(ItemStack itemStack) {
        itemStack.getOrCreateTag().putInt(CalamityHelp.FONT_FLAG, 4);
    }
}
