package hua223.calamity.mixins.client;

import hua223.calamity.render.Item.ICustomBackgroundRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderMixin {
    @Inject(method = "renderRectangle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIII)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void renderCustomizeBackground(GuiGraphics graphics, int x, int y, int width, int height,
                                                  int z, int colorForm, int colorTo, CallbackInfo ci) {
        if (graphics.tooltipStack != ItemStack.EMPTY && graphics.tooltipStack.getItem() instanceof ICustomBackgroundRender render) {
            render.render(graphics, x, y, x + width, y + height, z, colorForm, colorTo);
            ci.cancel();
        }
    }
}
