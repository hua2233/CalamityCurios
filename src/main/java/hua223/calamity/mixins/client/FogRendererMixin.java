package hua223.calamity.mixins.client;

import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FogRenderer.class})
public abstract class FogRendererMixin {
    @Inject(method = "getPriorityFogFunction", at = @At("RETURN"), cancellable = true)
    private static void canStoppedByRadiation(Entity entity, float p_234167_, CallbackInfoReturnable<?> cir) {
        if (cir.getReturnValue() != null && entity instanceof Player player && CalamityHelp.getCalamityFlag(player, 0))
            cir.setReturnValue(null);
    }
}