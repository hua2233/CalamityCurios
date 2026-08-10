package hua223.calamity.register.effects;

import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static hua223.calamity.generators.DamageMapping.*;

public class HolyFlames extends CalamityEffect {
    @DamageRequester(key = "holy_flames", tags = {BYPASSES_ARMOR, IS_FIRE,
        BYPASSES_ENCHANTMENTS, BYPASSES_EFFECTS, BYPASSES_COOLDOWN, BYPASSES_RESISTANCE},
        style = ChatFormatting.GOLD, zh_cn = "%s的灵魂被送进了无间地狱")
    public static DamageSupplier supplier;

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

        target.hurt(supplier.get(), damage);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("holy_flames").withStyle(ChatFormatting.GOLD));
    }
}
