package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = SprintCurio.class)
public class OrnateShield extends SprintCurio {
    public OrnateShield(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.source == DamageSource.FREEZE) listener.canceledEvent();
    }

    protected static void immuneSprint(ServerPlayer player, LivingEntity target, int damage, int time) {
        target.hurt(DamageSource.playerAttack(player), damage);
        player.invulnerableTime += time;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "ornate_shield", 4, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getTime() {
        return 4;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public int getCooldownTime() {
        return 100;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        immuneSprint(player, target, 5, 4);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ornate_shield", 1));
        tooltips.add(CMLangUtil.getTranslatable("ornate_shield", 2));
        tooltips.add(CMLangUtil.getTranslatable("ornate_shield", 3));
        return tooltips;
    }
}
