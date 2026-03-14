package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
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

public class Radiance extends BaseCurio implements ICuriosStorage {
    public Radiance(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 0, true);
        syncHealth(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 0, false);
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "radiance_bonus", 0.7, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "radiance", 8, AttributeModifier.Operation.ADDITION));
        
    }

    @Override
    public int getCountSize() {
        return 6;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 30) calculationLogic(player);
        if (addCount(player, 1) >= 100) healLogic(player);
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    private void calculationLogic(Player player) {
        var p = getPair(player);
        float[] floats = p.getA();
        floats[0] = 0;

        if (player.walkDistO == player.walkDist) {
            if (floats[3] < 20) floats[3] += 2;
        } else {
            floats[3] = 0;
        }

        int debuffCount = (int) player.getActiveEffectsMap().keySet().stream()
            .filter(effect -> !effect.isBeneficial()).count();

        if (debuffCount == floats[4]) return;

        AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
        if (debuffCount == 0) {
            VariableAttributeModifier.updateModifierInInstance(instance, p.getB()[0], 8);
            floats[5] = 0;
        } else {
            floats[5] = debuffCount;
            VariableAttributeModifier.updateModifierInInstance(instance, p.getB()[0], 28 + Math.max(0, debuffCount - 1) * 8);
        }

        floats[4] = debuffCount;//更新减益数量
    }

    private void healLogic(Player player) {
        float[] floats = getCount(player);
        floats[1] = 0;

        float missingHealth = player.getMaxHealth() - player.getHealth();
        if (missingHealth != 0) {
            float healAmount = Math.min(floats[5] + floats[3], missingHealth);
            player.heal(healAmount);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style yellow = Style.EMPTY.withColor(ChatFormatting.YELLOW);
        tooltips.add(CMLangUtil.getTranslatable("radiance", 2).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("radiance", 3).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("radiance", 4).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("radiance", 5).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("radiance", 6).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("radiance", 7).setStyle(yellow));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("radiance", 1).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
