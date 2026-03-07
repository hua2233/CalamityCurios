package hua223.calamity.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import hua223.calamity.render.CurseTooltipExtensions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique
    private PoseStack calamity$TempPos;
    @Unique
    private boolean calamity$FirstLineText;

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void getPose(PoseStack poseStack, List<ClientTooltipComponent> pClientTooltipComponents, int pMouseX, int pMouseY, CallbackInfo ci) {
        calamity$TempPos = poseStack;
    }

    @Redirect(method = "renderTooltipInternal", at = @At(value = "INVOKE",
        target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private Object getIndex(List<ClientTooltipComponent> instance, int i) {
        calamity$FirstLineText = i == 0;
        return instance.get(i);
    }

    @Redirect(method = "renderTooltipInternal", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;renderText(Lnet/minecraft/client/gui/Font;IILcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"))
    private void renderCurseText(ClientTooltipComponent instance, Font font, int x, int y,
                                 Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        if (calamity$FirstLineText && instance instanceof CurseTooltipExtensions component)
            component.render(calamity$TempPos, x, y);
        else instance.renderText(font, x, y, matrix4f, bufferSource);
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void removePos(PoseStack pPoseStack, List<ClientTooltipComponent> pClientTooltipComponents, int pMouseX, int pMouseY, CallbackInfo ci) {
        calamity$TempPos = null;
    }
}
