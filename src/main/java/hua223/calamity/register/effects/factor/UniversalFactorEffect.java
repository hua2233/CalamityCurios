package hua223.calamity.register.effects.factor;

import com.mojang.serialization.Codec;
import hua223.calamity.register.effects.CalamityEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public abstract class UniversalFactorEffect<E, T extends UniversalFactorEffect.UniversalFactor<E>>
    extends CalamityEffect implements IFactorFactory<E, T> {
    protected UniversalFactorEffect(MobEffectCategory category, int color) {
        super(category, color);
        setFactorDataFactory(this::factory);
    }

    public static Optional<? extends MobEffectInstance.FactorData> fromNbt(CompoundTag tag) {
        ResourceLocation id = identifierDeserialization(tag);
        UniversalFactorEffect<?, ?> universal = ((UniversalFactorEffect<?, ?>) ForgeRegistries.MOB_EFFECTS.getValue(id));

        Optional<UniversalFactorEffect.UniversalFactor<?>> optional =
            (Optional<UniversalFactorEffect.UniversalFactor<?>>) universal.load(tag);

        //Delay setting dependencies here
        optional.get().setDependence(universal);
        return optional;
    }

    protected T fromTargetGet(LivingEntity entity) {
        return entity.getEffect(this).calamity$GetUniversalFactor(this);
    }

    private static ResourceLocation identifierDeserialization(CompoundTag tag) {
        ResourceLocation location = ResourceLocation.CODEC.parse(NbtOps.INSTANCE, tag.get("identifier")).result().get();
        tag.remove("identifier");
        return location;
    }

    protected Codec<T> codec() {
        throw new NoSuchElementException("Such factor are not coded using Codec!!");
    }

    public CompoundTag toNbt(Object f) {
        return identifierSerialization((CompoundTag) save((T) f));
    }

    private CompoundTag identifierSerialization(CompoundTag tag) {
        tag.put("identifier", ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE,
            ForgeRegistries.MOB_EFFECTS.getKey(this)).result().get());
        return tag;
    }

    protected abstract T factory();

    public static class UniversalFactor<E> extends MobEffectInstance.FactorData {
        protected E factor;
        protected LivingEntity owner;
        private UniversalFactorEffect<E, ?> supplier;
        private BiConsumer<MobEffectInstance, UniversalFactor<E>> updater;
        private boolean staticFactor;

        public UniversalFactor(UniversalFactorEffect<E, ?> supplier) {
            super(0, 0, 0, 0, 0, 0, false);
            this.supplier = supplier;
            updater = (BiConsumer<MobEffectInstance, UniversalFactor<E>>) supplier.createFactorUpdater();
            staticFactor = updater == null;
        }

        protected UniversalFactor(E factor) {
            super(0, 0, 0, 0, 0, 0, false);
            this.factor = factor;
        }

        public void initFactorData(MobEffectInstance instance) {
            factor = supplier.initFactorData().apply(instance);
        }

        public E getFactor() {
            return factor;
        }

        private void setDependence(UniversalFactorEffect<?, ?> supplier) {
            this.supplier = (UniversalFactorEffect<E, ?>) supplier;
            updater = (BiConsumer<MobEffectInstance, UniversalFactor<E>>) supplier.createFactorUpdater();
            staticFactor = updater == null;
        }

        @Override
        public void update(@NotNull MobEffectInstance instance) {
            if (!staticFactor) updater.accept(instance, this);
        }

        public LivingEntity getOwner() {
            return owner;
        }

        public void setOwner(LivingEntity owner) {
            this.owner = owner;
        }
    }
}
