package hua223.calamity.register.effects;

import hua223.calamity.events.LogoutRelease;
import hua223.calamity.register.config.AutoConfig;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static hua223.calamity.register.effects.Zerg.*;

public class Zen extends CalamityEffect implements IEffectsCallBack {
    static final int[] MOB_COUNT_AND_SPAWN_RATE_MODIFY = {0, 0};
    @AutoConfig(comment = "Zen potion reduces the probability of mob spawn",
        path = "ProbabilityOfInfluencingSpawn", defaultValue = {0f, 1})
    public final static double ZEN_RATE_AMPLIFIER = CalamityConfig.value(0.4);

    @AutoConfig(comment = "Zen potion reduces the number of mob spawn",
    path = "RatioOfInfluencingMaxNumber", defaultValue = {0f, 1})
    public final static double ZEN_NUMBER_AMPLIFIER = CalamityConfig.value(0.3);

    public Zen(MobEffectCategory category, int color) {
        super(category, color);
    }

    @LogoutRelease
    public static void remove(ServerPlayer player) {
        if (player.hasEffect(CalamityEffects.ZEN.get()))
            MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]--;

        if (player.hasEffect(CalamityEffects.ZERG.get()))
            MOB_COUNT_AND_SPAWN_RATE_MODIFY[1]--;
    }

    public static double getSpawnNumberAmplifier() {
        float zen = MOB_COUNT_AND_SPAWN_RATE_MODIFY[0];
        float zerg = MOB_COUNT_AND_SPAWN_RATE_MODIFY[1];
        return zen == zerg ? 1f : zen > zerg ? ZEN_NUMBER_AMPLIFIER : ZERG_NUMBER_AMPLIFIER;
    }

    public static boolean hasMobSpawnInfluence() {
        float zen = MOB_COUNT_AND_SPAWN_RATE_MODIFY[0];
        float zerg = MOB_COUNT_AND_SPAWN_RATE_MODIFY[1];
        return zen != zerg && (zerg > 0 || zen > 0);
    }

    public static boolean isZen() {
        return MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] != MOB_COUNT_AND_SPAWN_RATE_MODIFY[1] && MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] > 0f;
    }


    public static int getInterval() {
        return MOB_COUNT_AND_SPAWN_RATE_MODIFY[0] > MOB_COUNT_AND_SPAWN_RATE_MODIFY[1] ? 800 : 100;
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]++;
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer) MOB_COUNT_AND_SPAWN_RATE_MODIFY[0]--;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("zen").withStyle(ChatFormatting.WHITE));
    }
}
