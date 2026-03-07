package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TryToSniff.class})
public class SniffMixin {
    @Final
    @Shadow
    private static IntProvider SNIFF_COOLDOWN;

    @Inject(method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At("HEAD"), cancellable = true)
    private void preventSniffing(ServerLevel level, Warden warden, long gameTime, CallbackInfo cir) {
        Brain<Warden> brain = warden.getBrain();
        brain.getMemory(MemoryModuleType.NEAREST_ATTACKABLE).ifPresent(entity -> {
            if (CalamityCap.isCalamity(entity) && CalamityCap.isInverted(CalamityCap.CurseType.SILVA, entity)) {
                brain.setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, SNIFF_COOLDOWN.sample(level.getRandom()));
                cir.cancel();
            }
        });
    }
}
