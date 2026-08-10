package hua223.calamity.register.effects;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
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

public class Apoptosis extends CountFactorEffects implements IEffectsCallBack {
    protected Apoptosis(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "123e4567-e89b-12d3-a456-426614174028", 1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    protected CountFactor factory() {
        return new CountFactor(1) {
            @Override
            public void tick(@NotNull MobEffectInstance instance) {
                LivingEntity entity = getOwner();
                if (entity != null) {
                    float value = factor[0];

                    if (value > 0 && value < 60) {
                        entity.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment -> {
                            if (enchantment.getRunes() == SpellType.WITHERED) factor[0]++;
                        });
                    }

                    if (factor[0] == value)factor[0]--;
                }
            }
        };
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide) {
            float factor = fromTargetGet(livingEntity).getFactor()[0];
            livingEntity.hurt(livingEntity.damageSources().magic()
                , (float) (2 * Math.pow(1.5, (factor / 60))));
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
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        inactivationEffect(entity, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("apoptosis").withStyle(ChatFormatting.DARK_GRAY));
    }
}
