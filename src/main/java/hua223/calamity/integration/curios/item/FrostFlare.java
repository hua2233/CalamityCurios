package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.integration.curios.listeners.PlayerHealListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class FrostFlare extends BaseCurio implements ICuriosStorage {
    private static final UUID ID = UUID.randomUUID();

    public FrostFlare(Properties properties) {
        super(properties);
    }

    private static void onHealthChange(ServerPlayer player) {
        float heal = player.getHealth();
        float half = player.getMaxHealth() / 2;

        Attribute update;
        Attribute reset;
        float value;
        if (heal > half) {
            update = Attributes.ATTACK_DAMAGE;
            reset = Attributes.ARMOR;
            value = 0.1f;
        } else {
            update = Attributes.ARMOR;
            reset = Attributes.ATTACK_DAMAGE;
            value = 8f;
        }

        AttributeInstance instance = player.getAttribute(update);
        AttributeModifier modifier = instance.getModifier(ID);
        if (modifier.getAmount() == 0) {
            ((VariableAttributeModifier) modifier).setValue(value, instance);
            VariableAttributeModifier.updateModifierInInstance(player.getAttribute(reset), ID, 0);
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        onHealthChange(player);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.entity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            listener.entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        onHealthChange(listener.player);

        if (listener.source == DamageSource.FREEZE) listener.amplifier -= 0.4f;
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        onHealthChange(listener.player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED, new VariableAttributeModifier(ID, "frost_flare", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ATTACK_DAMAGE, new VariableAttributeModifier(ID, "frost_flare", 0, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(ID, "frost_flare", 0, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) == 10) {
            float[] count = getCount(player);
            count[0] = 0;
            if (player.isSprinting()) {
                if (count[1] < 0.15f) VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), ID, count[1] += 0.03f);
            } else if (count[1] != 0) {
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), ID, count[1] = 0);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("frost_flare", 1));
        tooltips.add(CMLangUtil.getTranslatable("frost_flare", 2));
        tooltips.add(CMLangUtil.getTranslatable("frost_flare", 3));
        tooltips.add(CMLangUtil.getTranslatable("frost_flare", 4));
        tooltips.add(CMLangUtil.getTranslatable("frost_flare", 5));
        return tooltips;
    }
}
