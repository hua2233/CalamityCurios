package hua223.calamity.mixins.client;

import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "renderLevel", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    private float setDistort(float value) {
        if (value == 0 || CalamityHelp.getCalamityFlag(minecraft.player, 0)) return 0;
        return value;
    }

    @Inject(method = "render(FJZ)V", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor3DItems()V", shift = At.Shift.AFTER))
    private void preRender(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        RenderUtil.Shaders.preScreenRender(partialTick, minecraft);
    }
}
