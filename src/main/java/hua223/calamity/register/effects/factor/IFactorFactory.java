package hua223.calamity.register.effects.factor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface IFactorFactory<E, T extends UniversalFactorEffect.UniversalFactor<E>> {
    default BiConsumer<MobEffectInstance, T> createFactorUpdater() {
        return null;
    }

    E initFactorData(MobEffectInstance instance);

    CompoundTag save(T factor);

    Optional<T> load(CompoundTag tag);
}