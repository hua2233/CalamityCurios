package hua223.calamity.mixins.client;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundUpdateMobEffectPacket.class)
public abstract class MobEffectPacketMixin {
    @Shadow
    @Mutable
    public byte flags;

    @Inject(method = "<init>(ILnet/minecraft/world/effect/MobEffectInstance;)V", at = @At(value = "RETURN"))
    private void save(int entityId, MobEffectInstance effectInstance, CallbackInfo ci) {
        if (effectInstance.calamity$NoFlicker) flags = (byte) (flags | (byte) -128);
    }
}
