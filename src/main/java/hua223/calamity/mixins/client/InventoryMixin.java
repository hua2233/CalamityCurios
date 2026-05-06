package hua223.calamity.mixins.client;

import hua223.calamity.util.RenderUtil;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void resetGuiEnchantRender(CallbackInfo ci) {
        RenderUtil.renderGuiEnchantParticle = false;
    }
}
