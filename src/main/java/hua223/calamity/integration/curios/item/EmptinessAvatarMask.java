package hua223.calamity.integration.curios.item;

import hua223.calamity.render.font.CalamityFont;
import hua223.calamity.render.item.IAvatarBackgroundRender;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmptinessAvatarMask extends Mask implements IAvatarBackgroundRender {
    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return CalamityFont.getAvatar(Component.translatable(getDescriptionId(stack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        super.getSlotsTooltip(tooltips, stack);
        tooltips.add(CMLangUtil.getTranslatable("avatar_mask").withStyle(ChatFormatting.DARK_RED));
        return tooltips;
    }
}
