package hua223.calamity.render;

import hua223.calamity.render.font.CurseFont;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public record CurseEnchantmentExtensions(CurseFont.FontFormat format) implements IClientItemExtensions {
    @Override
    public Font getFont(ItemStack stack, FontContext context) {
        return CurseFont.INSTANCE.setRenderFormat(format);
    }
}
