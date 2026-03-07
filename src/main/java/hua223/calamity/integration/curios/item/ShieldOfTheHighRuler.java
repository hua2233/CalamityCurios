package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = SprintCurio.class)
public class ShieldOfTheHighRuler extends SprintCurio {
    public ShieldOfTheHighRuler(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isFarAttack()) {
            Vec3 sourcePos = listener.source.getSourcePosition();
            if (sourcePos != null) {
                Vec3 view = listener.player.getViewVector(1.0F);
                Vec3 to = sourcePos.vectorTo(listener.player.position()).normalize();
                if (new Vec3(to.x, 0.0D, to.z).dot(view) < 0.0D)
                    listener.amplifier -= 0.15f;
            }
        }
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancelHarmfulOnes(0.5f);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "shield_of_the_high_ruler", 9.99, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "shield_of_the_high_ruler", 7, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getTime() {
        return 5;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public int getCooldownTime() {
        return 400;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        applyCounterforce(player);
        target.hurt(DamageSource.playerAttack(player), 7);

        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) {
            ItemCooldowns.CooldownInstance instance = cooldowns.cooldowns.get(this);
            if (instance != null) {
                int halfEnd = instance.startTime + getCooldownTime() / 2;
                int tickCount = cooldowns.tickCount;
                if (halfEnd > tickCount) {
                    cooldowns.addCooldown(this, halfEnd - tickCount);
                } else {
                    cooldowns.removeCooldown(this);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("shield_of_the_high_ruler", 2));
            tooltips.add(CMLangUtil.getTranslatable("shield_of_the_high_ruler", 3));
            tooltips.add(CMLangUtil.getTranslatable("shield_of_the_high_ruler", 4));
            tooltips.add(CMLangUtil.getTranslatable("shield_of_the_high_ruler", 5));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("shield_of_the_high_ruler", 1));
        }
        return tooltips;
    }
}
