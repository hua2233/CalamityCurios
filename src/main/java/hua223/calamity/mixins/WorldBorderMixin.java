package hua223.calamity.mixins;

import hua223.calamity.events.BossRushEvent;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {
    @Shadow private WorldBorder.BorderExtent extent;

    @Shadow private double centerX;

    @Shadow private double centerZ;

    @Shadow public abstract double getSize();

    @Shadow @Final public static WorldBorder.Settings DEFAULT_SETTINGS;

    @Inject(method = "setCenter", at = @At("HEAD"), cancellable = true)
    private void bossRushBox(double x, double z, CallbackInfo ci) {
        if (BossRushEvent.isBossRushEventActivating()) ci.cancel();
    }

    @Unique
    public void calamity$BossRushBox(double x, double z, double size) {
        if (size != 0) extent = ((WorldBorder)
            (Object) this).new StaticBorderExtent(size);
        if (x != Double.MAX_VALUE) {
            centerX = x;
            centerZ = z;
            extent.onCenterChange();
        }
    }

   @Inject(method = "createSettings", at = @At("HEAD"), cancellable = true)
   private void bossRushSettings(CallbackInfoReturnable<WorldBorder.Settings> cir) {
        if (BossRushEvent.isBossRushEventActivating()) cir.setReturnValue(DEFAULT_SETTINGS);
   }

    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void bossRushBox(double size, CallbackInfo ci) {
        if (BossRushEvent.isBossRushEventActivating()) ci.cancel();
    }
}
