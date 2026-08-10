package hua223.calamity.mixins;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AlchemistCauldronTile.class, remap = false)
public class AlchemistCauldronTileMixin {
    @Redirect(method = "tryMeltInput", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack blockFusion(ItemStack instance, int amount) {
        return instance.getCount() > 2 ? ItemStack.EMPTY : instance.split(amount);
    }

    @Redirect(method = "tryMeltInput", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack noSplit(ItemStack instance, int amount) {
        return instance;
    }
}
