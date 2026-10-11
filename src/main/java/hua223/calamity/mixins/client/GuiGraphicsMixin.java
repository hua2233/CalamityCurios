package hua223.calamity.mixins.client;

import hua223.calamity.mixed.ICalamityFontSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Inject(method = "renderTooltipInternal", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
        value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void allowRenderCalamityFont(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY,
                                         ClientTooltipPositioner tooltipPositioner, CallbackInfo ci, RenderTooltipEvent.Pre preEvent) {
        ((ICalamityFontSetting) preEvent).calamity$AllowGetFont();
    }
}
