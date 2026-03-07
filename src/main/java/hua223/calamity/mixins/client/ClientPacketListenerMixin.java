package hua223.calamity.mixins.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleUpdateMobEffect", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/effect/MobEffectInstance;setNoCounter(Z)V"))
    private void setAdditionalData(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci, Entity var1, MobEffect var2, MobEffectInstance locals) {
        locals.calamity$NoFlicker = packet.flags < 0;
    }
}
