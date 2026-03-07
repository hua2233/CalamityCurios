package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Evolution extends BaseCurio {
    public Evolution(Properties pProperties) {
        super(pProperties);
    }

    private static boolean matchingTag(Projectile projectile, CompoundTag tags) {
        return projectile.getType().getDescriptionId().equals(tags.getString("evolution"));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.getPersistentData().remove("evolution");
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.baseAmount <= player.getMaxHealth() * 0.1) return;

        if (listener.isFarAttack()) {
            Projectile projectile = listener.projectile;
            ItemCooldowns cooldowns = player.getCooldowns();
            CompoundTag tags = player.getPersistentData();
            if (cooldowns.isOnCooldown(this)) {
                if (matchingTag(projectile, tags)) listener.amplifier -= 0.25f;
            } else {
                projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-1.5));
                projectile.setOwner(player);
                projectile.calamity$Indestructible = true;
                int cooldownsTime = (int) Math.min(listener.baseAmount * 20, 900);
                cooldowns.addCooldown(this, cooldownsTime);
                player.heal(listener.baseAmount / 2);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1, 100));
                tags.putString("evolution", projectile.getType().getDescriptionId());
                listener.canceledEvent();
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("evolution", 1));
        tooltips.add(CMLangUtil.getTranslatable("evolution", 2));
        tooltips.add(CMLangUtil.getTranslatable("evolution", 3));
        tooltips.add(CMLangUtil.getTranslatable("evolution", 4));
        tooltips.add(CMLangUtil.getTranslatable("evolution", 5));
        return tooltips;
    }
}
