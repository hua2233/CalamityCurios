package hua223.calamity.register.effects.factor;

import com.mojang.serialization.Codec;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class UniversalFactorEffect<E, T extends UniversalFactorEffect.UniversalFactor<E>> extends CalamityEffect {
    protected UniversalFactorEffect(MobEffectCategory category, int color) {
        super(category, color);
        setFactorDataFactory(this::factory);
    }

    public abstract CompoundTag save(T factor);

    public abstract Optional<T> load(CompoundTag tag);

    @SuppressWarnings("ConstantConditions")
    public static Optional<? extends MobEffectInstance.FactorData> fromNbt(CompoundTag tag) {
        ResourceLocation id = CalamityCurios.resource(tag.getString("identifier"));
        UniversalFactorEffect<?, ?> universal = ((UniversalFactorEffect<?, ?>) ForgeRegistries.MOB_EFFECTS.getValue(id));
        return universal.load(tag);
    }

    @SuppressWarnings("ConstantConditions")
    protected T fromTargetGet(LivingEntity entity) {
        return entity.getEffect(this).calamity$GetUniversalFactor(this);
    }
    protected Codec<T> codec() {
        throw new NoSuchElementException("Such factor are not coded using Codec!!");
    }

    @SuppressWarnings("ConstantConditions")
    public CompoundTag toNbt(Object f) {
        CompoundTag tag = save((T) f);
        tag.putString("identifier", ForgeRegistries.MOB_EFFECTS.getKey(this).toString());
        return tag;
    }


    protected abstract T factory();

    public static class UniversalFactor<E> extends MobEffectInstance.FactorData {
        protected E factor;
        protected LivingEntity owner;

        protected UniversalFactor() {
            super(0, 0, 0, 0, 0, 0, false);
        }

        protected UniversalFactor(E factor) {
            super(0, 0, 0, 0, 0, 0, false);
            this.factor = factor;
        }

        public void initFactorData(MobEffectInstance instance) {
        }

        public E getFactor() {
            return factor;
        }

        @Override
        public void tick(@NotNull MobEffectInstance instance) {
        }

        public LivingEntity getOwner() {
            return owner;
        }

        public void setOwner(LivingEntity owner) {
            this.owner = owner;
        }
    }
}
