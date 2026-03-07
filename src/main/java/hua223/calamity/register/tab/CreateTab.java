package hua223.calamity.register.tab;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static hua223.calamity.register.Items.CalamityItems.YHARIM_GIFT;
import static hua223.calamity.register.Items.CalamityItems.ZENITH;

public class CreateTab {
    public static final CreativeModeTab CALAMITY_ITEM = new CreativeModeTab("calamity_item") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ZENITH.get());
        }
    };

    public static final CreativeModeTab CALAMITY_CURIOS = new CreativeModeTab("calamity_curios") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(YHARIM_GIFT.get());
        }
    };
}
