package hua223.calamity.mixins;

import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeModifier.class)
public class AttributeModifierMixin {
    @Inject(method = "load", at = @At(value = "RETURN"), cancellable = true)
    private static void loadVariable(CompoundTag nbt, CallbackInfoReturnable<AttributeModifier> cir) {
        if (nbt.getBoolean("Variable")) {
            AttributeModifier modifier = cir.getReturnValue();
            cir.setReturnValue(new VariableAttributeModifier(modifier.getId(), modifier.getName(), modifier.getAmount(), modifier.getOperation()));
        }
    }
}
