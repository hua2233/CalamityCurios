package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static hua223.calamity.register.tab.CreateTab.CALAMITY_ITEM;

public class CalamityRecords extends RecordItem {
    private static final Item.Properties RECORD_RARE =
        new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.RARE).stacksTo(1);

    public CalamityRecords(Supplier<SoundEvent> soundSupplier, int secCount) {
        super(13, soundSupplier, RECORD_RARE, secCount * 20);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(CMLangUtil.getTranslatable("calamity_records"));
    }
}
