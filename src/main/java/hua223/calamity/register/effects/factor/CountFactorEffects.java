package hua223.calamity.register.effects.factor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CountFactorEffects extends UniversalFactorEffect<float[], CountFactorEffects.CountFactor> {
    protected CountFactorEffects(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public Tag save(CountFactor factor) {
        return codec().encodeStart(NbtOps.INSTANCE, factor).result().orElse(null);
    }

    @Override
    public Optional<CountFactor> load(CompoundTag tag) {
        return codec().parse(new Dynamic<>(NbtOps.INSTANCE, tag)).result();
    }

    @Override
    protected Codec<CountFactor> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(Codec.list(Codec.FLOAT).fieldOf("data").forGetter(
            CountFactor::toObjectList)).apply(instance, CountFactor::new));
    }

    @Override
    protected CountFactor factory() {
        return new CountFactor(this);
    }

    public static class CountFactor extends UniversalFactor<float[]> {
        public CountFactor(UniversalFactorEffect<float[], CountFactor> supplier) {
            super(supplier);
        }

        protected CountFactor(float[] factor) {
            super(factor);
        }

        private CountFactor(List<Float> floats) {
            this(fromObjectList(floats));
        }

        private static float[] fromObjectList(List<Float> floats) {
            if (floats.isEmpty()) return null;

            float[] d1 = new float[floats.size()];
            for (int i = 0; i < floats.size(); i++)
                d1[i] = floats.get(i);

            return d1;
        }

        @Override
        public float getFactor(@NotNull LivingEntity entity, float f) {
            return factor[0];
        }

        public float getRatio(float f, int index) {
            return f / factor[index];
        }

        private List<Float> toObjectList() {
            List<Float> floats = new ArrayList<>(factor.length);

            for (float d : factor)
                floats.add(d);

            return floats;
        }
    }
}
