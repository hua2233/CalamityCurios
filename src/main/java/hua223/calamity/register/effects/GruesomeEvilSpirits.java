package hua223.calamity.register.effects;

import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GruesomeEvilSpirits extends CountFactorEffects {
    protected GruesomeEvilSpirits(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "123e4567-e89b-12d3-a456-426614174027",
            1.5, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    private static Vec2 getRandomVector(RandomSource random, float scale) {
        double angle = random.nextDouble() * Math.PI * 2;
        double magnitude = random.nextDouble() * scale;

        return new Vec2((float) (magnitude * Math.cos(angle)),
            (float) (magnitude * Math.sin(angle)));
    }

    @Override
    public BiConsumer<MobEffectInstance, CountFactor> createFactorUpdater() {
        return ((instance, factor) -> {
            int duration = instance.getDuration();
            if (duration > 0 && duration % 20 == 0) {
                factor.getFactor()[1] = factor.getFactor()[0] / duration;
            }
        });
    }

    @Override
    public Function<MobEffectInstance, float[]> initFactorData() {
        return (instance -> new float[]{instance.getDuration(), 0});
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level.isClientSide) {
            float factor = livingEntity.getEffect(this).calamity$GetUniversalFactor(this).getFactor()[1];
            RandomSource source = livingEntity.getRandom();
            Vec2 vec = getRandomVector(source, factor);
            livingEntity.moveTo(livingEntity.position().add(vec.x, 0, vec.y));

            float angleX = source.nextFloat() * 2 - 1;
            float angleY = source.nextFloat() * 2 - 1;
            float angleZ = source.nextFloat() * 2 - 1;
            Vec3 offset = new Vec3(angleX, angleY, angleZ).scale(1 - factor);
            livingEntity.lookAt(EntityAnchorArgument.Anchor.EYES, livingEntity.getEyePosition().add(offset));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 1).withStyle(ChatFormatting.DARK_RED));
        tooltips.add(CMLangUtil.getTranslatable("gruesome_eminence", 2).withStyle(ChatFormatting.DARK_RED));
    }
}
