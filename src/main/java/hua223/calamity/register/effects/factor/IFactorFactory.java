package hua223.calamity.register.effects.factor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IFactorFactory<E, T extends UniversalFactorEffect.UniversalFactor<E>> {
    BiConsumer<MobEffectInstance, T> createFactorUpdater();

    Function<MobEffectInstance, E> initFactorData();

    Tag save(T factor);

    Optional<T> load(CompoundTag tag);
}