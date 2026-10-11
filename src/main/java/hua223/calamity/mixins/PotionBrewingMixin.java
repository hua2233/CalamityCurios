package hua223.calamity.mixins;

import hua223.calamity.register.recipe.brewing.CalamityPotionBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {
    @Inject(method = "isIngredient", at = @At("RETURN"), cancellable = true)
    private static void calamityBrewingIngredient(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && CalamityPotionBrewing.isCalamityIngredient(input))
            cir.setReturnValue(Boolean.TRUE);
    }

    @Inject(method = "hasMix", at = @At("RETURN"), cancellable = true)
    private static void calamityBrewingMix(ItemStack input, ItemStack reagent, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && CalamityPotionBrewing.hasCalamityMix(input, reagent))
            cir.setReturnValue(Boolean.TRUE);
    }

    @Inject(method = "mix", at = @At("TAIL"), cancellable = true)
    private static void calamityMix(ItemStack reagent, ItemStack potion, CallbackInfoReturnable<ItemStack> cir) {
        if (!potion.isEmpty()) cir.setReturnValue(CalamityPotionBrewing.tryCraft(reagent, potion));
    }
}
