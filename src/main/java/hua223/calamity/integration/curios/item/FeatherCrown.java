package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FeatherCrown extends BaseCurio {
    public FeatherCrown(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.addCallbackAfterCriticalHit(() -> {
            if (!listener.target.hasEffect(MobEffects.LEVITATION))
                listener.target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));
        });
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += 0.15f;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.fallDistance > 3 && !player.hasEffect(MobEffects.SLOW_FALLING))
            player.addEffect(new SurvivableEffectInstance(MobEffects.SLOW_FALLING,
                1000, 0, () -> !player.isOnGround()));
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("feather_crown", 1));
        tooltips.add(CMLangUtil.getTranslatable("feather_crown", 2));
        tooltips.add(CMLangUtil.getTranslatable("feather_crown", 3));
        return tooltips;
    }
}
