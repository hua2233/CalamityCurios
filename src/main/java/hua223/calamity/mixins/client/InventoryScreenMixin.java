package hua223.calamity.mixins.client;

import hua223.calamity.render.EnchantedParticleSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    public InventoryScreenMixin(InventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void setEnchanted(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EnchantedParticleSet.canUpdate = true;
        EnchantedParticleSet.isInventory = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void closeEnchanted(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EnchantedParticleSet.isInventory = false;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;init()V"))
    private void initEnchanted(CallbackInfo ci) {
        EnchantedParticleSet.initializationParticlePool();
    }

    @Unique
    public void removed() {
        if (minecraft.player != null) {
            EnchantedParticleSet.close();
            menu.removed(minecraft.player);
        }
    }
}
