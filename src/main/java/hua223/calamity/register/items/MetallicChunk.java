package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.render.font.CalamityFont;
import hua223.calamity.render.item.IAvatarBackgroundRender;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//A proof, in a sense. Similar to Rock
public class MetallicChunk extends Item implements IAvatarBackgroundRender {
    public MetallicChunk() {
        super(RegisterList.CALAMITY_ONE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return CalamityFont.getAvatar(Component.translatable(getDescriptionId(stack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("metallic_chunk").withStyle(ChatFormatting.GOLD));
    }
}
