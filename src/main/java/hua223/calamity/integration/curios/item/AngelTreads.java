package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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

@ConflictChain(value = Wings.class, isRoot = true)
public class AngelTreads extends Wings {
    public AngelTreads(Properties properties) {
        super(properties, 200, 1.15f, 1.1f);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED, new VariableAttributeModifier(uuid, "angel_treads", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        CalamityHelp.setCalamityFlag(player, 8, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        CalamityHelp.setCalamityFlag(player, 8, false);
    }

    @Override
    protected void onPlayerTick(Player player) {
        super.onPlayerTick(player);

        if (player.onGround() && player.getDeltaMovement().length() > 0) {
            var memory = getMemory(player);
            if (memory.count[2]++ == 10) {
                memory.count[2] = 0;
                if (player.isSprinting()) {
                    if (memory.count[3] < 0.36f) VariableAttributeModifier.updateModifierInInstance(
                        player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], memory.count[3] += 0.03f);
                } else if (memory.count[3] != 0) {
                    VariableAttributeModifier.updateModifierInInstance(
                        player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], memory.count[3] = 0);
                }

                if (player.getBlockStateOn().is(BlockTags.ICE)) {
                    if (memory.count[4] == 0) VariableAttributeModifier.updateModifierInInstance(
                        player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], memory.count[4] = 0.2f);
                } else if (memory.count[4] != 0) {
                    VariableAttributeModifier.updateModifierInInstance(
                        player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], memory.count[4] = 0f);
                }
            }
        }
    }

    @Override
    public int getCountSize() {
        return 5;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.YELLOW, "angel_treads", 2, 3, 4, 5, 6);
        super.getSlotsTooltip(tooltips, stack);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("angel_treads", 1).withStyle(ChatFormatting.YELLOW));
        return tooltips;
    }
}
