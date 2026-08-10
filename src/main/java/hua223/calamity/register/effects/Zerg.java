package hua223.calamity.register.effects;

import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.config.AutoConfig;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Zerg extends CalamityEffect implements IEffectsCallBack {
    @AutoConfig(comment = "The probability of successfully spawning an additional mob per zerg potion attempt",
        path = "TheProbabilityOfAdditionalMobSpawns", defaultValue = {0, 1})
    public static final double ZERG_RATE_AMPLIFIER = CalamityConfig.value(0.5);

    @AutoConfig(comment = "The multiplier by which the Zerg potion affects the world's maximum mob cap",
        path = "MaximumMobCapMultiplier", defaultValue = {1, 7})
    public static final double ZERG_NUMBER_AMPLIFIER = CalamityConfig.value(3);

    @AutoConfig(comment = "How many additional times can the Zerg potion check for spawns",
        path = "AdditionalSpawnAttempts", defaultValue = {0, 7})
    public static final int ZERG_SPAWN_COUNT = CalamityConfig.value(1);

    public Zerg(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer)
            Zen.MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]++;
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer)
            Zen.MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]--;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("zerg").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
