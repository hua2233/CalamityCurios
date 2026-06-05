package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipItem extends Item {
    private final int lien;
    private final String name;


    public TooltipItem(Properties properties, String name, int lien) {
        super(properties);
        this.lien = lien;
        this.name = name;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag avanced) {
        Style style = Style.EMPTY.withColor(stack.getRarity().color);
        for (int i = 0; i < lien; ) tooltips.add(CMLangUtil.getTranslatable(name, ++i).setStyle(style));
    }

}
