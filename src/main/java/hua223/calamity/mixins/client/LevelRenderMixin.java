package hua223.calamity.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import hua223.calamity.events.ClientRushEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRenderMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow private VertexBuffer skyBuffer;

    @Shadow private VertexBuffer darkBuffer;

    @Shadow @Nullable private ClientLevel level;

    @Inject(method = "renderSky", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target =
        "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private void bossRushSky(PoseStack poseStack, Matrix4f projectionMatrix, float
        partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if (ClientRushEvent.isBossRushEventActivating()) {
            ClientRushEvent.BossRushSky.renderSky(poseStack, minecraft, projectionMatrix,
                partialTick, skyBuffer, darkBuffer, level);
            ci.cancel();
        }
    }

    @Redirect(method = "renderWorldBorder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getEffectiveRenderDistance()I"))
    private int setDistance(Options instance) {
        return ClientRushEvent.isBossRushEventActivating() ? 1 : instance.getEffectiveRenderDistance();
    }
}
