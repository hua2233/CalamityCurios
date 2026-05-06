package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitCheckListener;
import hua223.calamity.integration.curios.listeners.CriticalHitTriggerListener;
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
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.player.getCooldowns().addCooldown(this, 40);
        LunarFlare.create(listener.player, listener.target.getBoundingBox().getCenter());
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        listener.probability += 0.03f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.AQUA, "moonstone_crown", 1, 2);
        return tooltips;
    }
}
