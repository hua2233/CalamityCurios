package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.clientInfos.Rage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(Community.class)
public class ShatteredCommunity extends BaseCurio implements ICuriosStorage {
    public ShatteredCommunity(Properties properties) {
        super(properties);
    }

    @ApplyEvent(1200)
    public final void onAttack(PlayerAttackListener listener) {
        final float amount = Math.min(3, listener.baseAmount / 7);
        CalamityCapProvider.RAGE.getCapabilityFrom(listener.player).ifPresent(rage -> {
            rage.addValue(amount, listener.player);
            if (rage.isActive()) {
                listener.amplifier += rage.getLevelBonus();
                rage.addLevelUpProgress((int) listener.getCorrectionValue(), listener.player);
            }
        });
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "RAGE_ACTIVE", true);
        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage -> rage.setEnabled(true, player));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "RAGE_ACTIVE", false);
        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage -> {
            rage.setRageValue(0, player);
            rage.setEnabled(false, player);
        });
        syncHealth(player);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 200) {
            zeroCount(player, 0);
            float maxHealth = player.getMaxHealth();
            if (player.getHealth() < maxHealth) {
                player.heal(maxHealth * 0.15f);
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "shattered", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "shattered", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new AttributeModifier(
            uuid, "shattered", (10 + equipped.getAttributeValue(Attributes.ARMOR) * 0.2), AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("shattered", 2));
            tooltips.add(CMLangUtil.getTranslatable("shattered", 3));
            tooltips.add(CMLangUtil.getTranslatable("shattered", 4));
            tooltips.add(CMLangUtil.getTranslatable("shattered", 5));
            tooltips.add(CMLangUtil.getDynamic("shattered", 6,
                Rage.ShatteredLevel).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getDynamic("shattered", 7,
                Rage.levelUpProgress).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getDynamic("shattered", 8,
                Rage.levelBonus).withStyle(ChatFormatting.GOLD));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("shattered", 1)
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return tooltips;
    }
}