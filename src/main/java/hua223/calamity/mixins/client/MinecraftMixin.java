package hua223.calamity.mixins.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @NotNull
    public LocalPlayer player;
    @Shadow
    protected int missTime;
    @Unique
    private boolean calamity$hasAttacked;

    @Inject(method = "startAttack", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    private void resetAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player.Calamity$Player.noAttackCooling) {
            missTime = 0;
            player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 13))
    private boolean canAttack(KeyMapping instance) {
        //Make sure it's a per-frame attack and not stuck
        if (!calamity$hasAttacked && player.Calamity$Player.noAttackCooling && instance.isDown())
            return calamity$hasAttacked = true;

        return instance.consumeClick();
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void liftAttackRestrictions(CallbackInfo ci) {
        calamity$hasAttacked = false;
    }
}
