package hua223.calamity.register.Items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnsealingRope extends Item {
    private final String text;

    public UnsealingRope(String text) {
        super(RegisterList.UNCOMMON_ONE);
        this.text = text;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable(text).withStyle(ChatFormatting.GOLD));
    }
}
