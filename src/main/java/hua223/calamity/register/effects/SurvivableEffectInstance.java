package hua223.calamity.register.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public class SurvivableEffectInstance extends MobEffectInstance {
    private BooleanSupplier survivalConditions;
    private MobEffect[] instances = {getEffect()};

    public SurvivableEffectInstance(MobEffect effect, int duration, int amplifier, BooleanSupplier conditions) {
        super(effect, duration, amplifier);
        if (conditions == null) throw new IllegalArgumentException("Why would I do this?");
        survivalConditions = conditions;
    }

    public SurvivableEffectInstance setNoFlicker() {
        calamity$NoFlicker = true;
        return this;
    }

    @Override
    public boolean update(@NotNull MobEffectInstance other) {
        boolean result = super.update(other);
        //If it is updated by any effect, it will be replaced with a default instance
        //But if it itself is overwritten by another SurvivableEffectInstance,
        //then delegate the condition to this one, regardless of whether it becomes inactive or not
        if (result) {
            if (other instanceof SurvivableEffectInstance instance) {
                survivalConditions = instance.survivalConditions;
                calamity$NoFlicker = instance.calamity$NoFlicker;
            } else survivalConditions = null;
        }

        return result;
    }

    public MobEffectInstance addSubEffects(MobEffect... effects) {
        if (instances.length == 1) {
            MobEffect[] _new = new MobEffect[effects.length + instances.length];
            System.arraycopy(instances, 0, _new, 0, instances.length);
            System.arraycopy(effects, 0, _new, instances.length, effects.length);
            instances =_new;
        }

        return this;
    }

    @Override
    public boolean tick(@NotNull LivingEntity entity, @NotNull Runnable runnable) {
        int d = getDuration();
        if (d > 0 && (survivalConditions == null || survivalConditions.getAsBoolean())) {
            int a = getAmplifier();
            for (MobEffect effect : instances)
                if (effect.isDurationEffectTick(d, a))
                    effect.applyEffectTick(entity, a);
            calamity$SetProperties(--d, 0, null);
            getFactorData().ifPresent(data -> data.tick(this));
            return true;
        }

        return false;
    }
}
