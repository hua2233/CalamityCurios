package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import jdk.dynalink.Operation;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin({TryToSniff.class})
public class SniffMixin {
    @Final
    @Shadow
    private static IntProvider SNIFF_COOLDOWN;

    /**
     * @author hua223
     * @reason correctionTry
     */
    @Overwrite
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create((instance) -> instance.group(instance.registered(MemoryModuleType.IS_SNIFFING),
            instance.registered(MemoryModuleType.WALK_TARGET), instance.absent(MemoryModuleType.SNIFF_COOLDOWN),
            instance.present(MemoryModuleType.NEAREST_ATTACKABLE), instance.absent(MemoryModuleType.DISTURBANCE_LOCATION))
            .apply(instance, (isSniffing, walkTarget, sniffCooldown,
                              nearestAttackable, disturbanceLocation) ->
                (level, entity, time) -> {
                Warden warden = (Warden) entity;
                Optional<LivingEntity> optional = warden.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
                sniffCooldown.setWithExpiry(Unit.INSTANCE, SNIFF_COOLDOWN.sample(level.getRandom()));
                if (optional.isPresent() && CalamityCap.isCalamity(optional.get())
                    && CalamityCap.isInverted(CalamityCap.CurseType.SILVA, optional.get())) return false;
                isSniffing.set(Unit.INSTANCE);
                walkTarget.erase();
                entity.setPose(Pose.SNIFFING);
                return true;
        }));
    }
}
