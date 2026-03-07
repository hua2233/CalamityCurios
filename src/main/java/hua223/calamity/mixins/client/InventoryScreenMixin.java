package hua223.calamity.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.render.EnchantedParticleSet;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void setEnchanted(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EnchantedParticleSet.canUpdate = true;
        EnchantedParticleSet.isInventory = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void closeEnchanted(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EnchantedParticleSet.isInventory = false;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;init()V"))
    private void initEnchanted(CallbackInfo ci) {
        EnchantedParticleSet.initializationParticlePool();
    }

    @Inject(method = "removed", at = @At("RETURN"))
    private void closeEnchanted(CallbackInfo ci) {
        EnchantedParticleSet.close();
    }
}
