package hua223.calamity.mixins.client;

import hua223.calamity.events.levelevent.client.ClientLevelEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "getSkyDarken", at = @At("HEAD"), cancellable = true)
    private void setSkyDarken(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (ClientLevelEvent.getActiveWorldEvent() != null)
            cir.setReturnValue(ClientLevelEvent.getActiveWorldEvent().getSkyDarkenValue(partialTick));
    }
}
