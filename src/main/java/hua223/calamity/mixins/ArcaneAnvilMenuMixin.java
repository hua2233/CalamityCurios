package hua223.calamity.mixins;

import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.rune.ArmorRune;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArcaneAnvilMenu.class)
public abstract class ArcaneAnvilMenuMixin extends ItemCombinerMenu {
    public ArcaneAnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1, shift = At.Shift.AFTER))
    private void afterScrollUpgrade(CallbackInfo ci, ItemStack result, ItemStack baseItemStack, ItemStack modifierItemStack) {
        if (modifierItemStack.is(CalamityItems.PREFIX_CLEAR_KNIFE.get()))
            ArmorRune.removeOrApplyRune(baseItemStack, null, resultSlots, ci);
        else if (modifierItemStack.getItem() instanceof ArmorRune rune)
            ArmorRune.removeOrApplyRune(baseItemStack, rune, resultSlots, ci);
    }
}
