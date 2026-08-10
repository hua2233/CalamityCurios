package hua223.calamity.mixins;

import hua223.calamity.events.levelevent.BossRushEvent;
import hua223.calamity.events.levelevent.LevelEvent;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    @Unique public VoxelShape calamity$Shape;

    @Inject(method = "setCenter", at = @At("HEAD"), cancellable = true)
    private void bossRushBox(double x, double z, CallbackInfo ci) {
        if (LevelEvent.inProgress(BossRushEvent.class)) ci.cancel();
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
        if (LevelEvent.inProgress(BossRushEvent.class)) cir.setReturnValue(DEFAULT_SETTINGS);
   }

    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void bossRushBox(double size, CallbackInfo ci) {
        if (LevelEvent.inProgress(BossRushEvent.class)) ci.cancel();
    }
}
