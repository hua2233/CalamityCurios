package hua223.calamity.render;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public record CurseEnchantmentExtensions(CurseFont font) implements IClientItemExtensions {
    @Override
    public @Nullable Font getFont(ItemStack stack, FontContext context) {
        return font;
    }
}
