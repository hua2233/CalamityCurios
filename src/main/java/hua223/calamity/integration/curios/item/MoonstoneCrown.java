package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.entity.LunarFlare;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class MoonstoneCrown extends BaseCurio {
    public MoonstoneCrown(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += 0.15f;
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.probability += 0.03f;
        listener.addCallbackAfterCriticalHit(() -> {
            if (!listener.player.getCooldowns().isOnCooldown(this)) {
                listener.player.getCooldowns().addCooldown(this, 20);
                LunarFlare.create(listener.player, listener.target.getBoundingBox().getCenter());
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("moonstone_crown", 1).withStyle(ChatFormatting.AQUA));
        tooltips.add(CMLangUtil.getTranslatable("moonstone_crown", 2).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
