package hua223.calamity.mixins.client;

import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "getDarknessGamma", at = @At("RETURN"), cancellable = true)
    private void canStoppedByRadiation(float factor, CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() != 0 && CalamityHelp.getCalamityFlag(minecraft.player, 0))
            cir.setReturnValue(0f);
    }
}
