package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HolyFlames extends CalamityEffect {
    public HolyFlames(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        float maxHeath = target.getMaxHealth();
        float damage = amplifier + 1f;
        if (maxHeath > 100) damage *= (maxHeath * 0.02f);
        else damage *= 2f;

        target.hurt(CalamityDamageSource.source(CalamityDamageTypes.HOLY_FLAMES, target.level()), damage);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("holy_flames").withStyle(ChatFormatting.GOLD));
    }
}
