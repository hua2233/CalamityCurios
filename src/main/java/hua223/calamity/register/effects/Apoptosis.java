package hua223.calamity.register.effects;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Apoptosis extends CountFactorEffects implements IEffectsCallBack {
    protected Apoptosis(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "123e4567-e89b-12d3-a456-426614174028", 1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public BiConsumer<MobEffectInstance, CountFactor> createFactorUpdater() {
        return (instance, factor) -> {
            LivingEntity entity = factor.getOwner();
            if (entity != null) {
                float[] values = factor.getFactor();
                float value = values[0];

                if (value > 0 && value < 60) {
                    entity.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment -> {
                        if (enchantment.getRunes() == SpellType.WITHERED) values[0]++;
                    });
                }

                if (values[0] == value) values[0]--;
            }
        };
    }

    @Override
    public Function<MobEffectInstance, float[]> initFactorData() {
        return instance -> new float[1];
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level.isClientSide) {
            MobEffectInstance instance = livingEntity.getEffect(this);
            float factor = instance.calamity$GetUniversalFactor(this).getFactor()[0];
            livingEntity.hurt(DamageSource.MAGIC, (float) (2 * Math.pow(1.5, (factor / 60))));
        }
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity instanceof ServerPlayer player) {
            player.calamity$InactivationCount++;
            effect.calamity$GetUniversalFactor(this).setOwner(entity);
        }

    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        inactivationEffect(entity, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("apoptosis").withStyle(ChatFormatting.DARK_GRAY));
    }
}
