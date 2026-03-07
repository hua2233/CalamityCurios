package hua223.calamity.register.effects.factor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated(since = "It has been implemented as a generic and is not compatible with the current mixin")
public abstract class CountFactorEffect extends MobEffect {
    public CountFactorEffect(MobEffectCategory category, int color) {
        super(category, color);
        setFactorDataFactory(() -> new CountFactorData(this));
    }

    protected abstract BiConsumer<MobEffectInstance, float[]> createFactorUpdater();

    protected abstract Function<MobEffectInstance, float[]> initFactorData();

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
    }

    //原版这几乎是只为特定效果(DARKNESS)实现，不过至少它有一个能够跟踪MobEffectInstance的办法，虽然不那么方便......
    //我曾有意愿将其设置为泛型类以拓展它的用途，但我发现我只需要一些简单的基元数据。所以，这个想法就被暂时搁置了（懒，(不是)）
    public static class CountFactorData extends MobEffectInstance.FactorData {
        public static final Codec<CountFactorData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.list(Codec.FLOAT).fieldOf("data").forGetter(CountFactorData::toObjectList),
                ResourceLocation.CODEC.fieldOf("identifier").forGetter(CountFactorData::getId)).apply(instance, CountFactorData::new));
        public final boolean fromDisk;
        private final BiConsumer<MobEffectInstance, float[]> updater;
        private final CountFactorEffect supplier;
        private final boolean staticFactor;
        private float[] data;

        private CountFactorData(List<Float> floats, ResourceLocation id) {
            super(0, 0, 0, 0, 0, 0, false);
            fromDisk = true;
            data = fromObjectList(floats);
            supplier = ((CountFactorEffect) ForgeRegistries.MOB_EFFECTS.getValue(id));
            updater = supplier.createFactorUpdater();
            staticFactor = updater == null;
        }

        public CountFactorData(CountFactorEffect supplier) {
            super(0, 0, 0, 0, 0, 0, false);
            fromDisk = false;
            this.supplier = supplier;
            updater = supplier.createFactorUpdater();
            staticFactor = updater == null;
        }

        private static float[] fromObjectList(List<Float> floats) {
            if (floats.isEmpty()) return null;

            float[] d1 = new float[floats.size()];
            for (int i = 0; i < floats.size(); i++)
                d1[i] = floats.get(i);

            return d1;
        }

        public void initFactorData(MobEffectInstance instance) {
            data = supplier.initFactorData().apply(instance);
        }

        private ResourceLocation getId() {
            return ForgeRegistries.MOB_EFFECTS.getKey(supplier);
        }

        @Override
        public void update(@NotNull MobEffectInstance instance) {
            if (!staticFactor) updater.accept(instance, data);
        }

        @Override
        public float getFactor(@NotNull LivingEntity entity, float f) {
            return data[0];
        }

        public float getProportionalData(float f, int index) {
            return f / data[index];
        }

        private List<Float> toObjectList() {
            List<Float> floats = new ArrayList<>(data.length);

            for (float d : data)
                floats.add(d);

            return floats;
        }
    }
}
