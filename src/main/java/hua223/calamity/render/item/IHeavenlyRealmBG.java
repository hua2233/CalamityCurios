package hua223.calamity.render.item;

import hua223.calamity.render.shaders.CalamityHeavenlyRealmShader;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHeavenlyRealmBG extends ICustomBackgroundRender, IBackwardsCompatDefaultNbtItem {
    @Override
    @OnlyIn(Dist.CLIENT)
    default void render(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int z, int colorForm, int colorTo) {
        CalamityHeavenlyRealmShader.drawHeavenlyRealmBackground(guiGraphics.pose().last().pose(), x1, y1, x2, y2, z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default RenderType getRenderType() {
        throw new UnsupportedOperationException();
    }


    @Override
    default void setupItem(ItemStack itemStack) {
        itemStack.getOrCreateTag().putInt(CalamityHelp.FONT_FLAG, 3);
    }
}
