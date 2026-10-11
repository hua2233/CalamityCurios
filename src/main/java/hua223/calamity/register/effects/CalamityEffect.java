package hua223.calamity.register.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CalamityEffect extends MobEffect {
    //Why is its access permission protected？
    public CalamityEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    protected String getSelfUUID() {
        return UUID.nameUUIDFromBytes(getClass().getName().getBytes()).toString();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        if (!target.level().isClientSide) applyServerTick(target, amplifier);
    }

    protected void applyServerTick(LivingEntity target, int amplifier) {

    }

    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
    }
}
