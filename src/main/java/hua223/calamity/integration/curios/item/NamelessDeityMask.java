package hua223.calamity.integration.curios.item;

import hua223.calamity.render.item.IHeavenlyRealmBG;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NamelessDeityMask extends Mask implements IHeavenlyRealmBG {
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        super.getSlotsTooltip(tooltips, stack);
        tooltips.add(CMLangUtil.getTranslatable("deity_mask").withStyle(ChatFormatting.YELLOW));
        return tooltips;
    }
}
