package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.*;
import net.minecraft.client.gui.screens.Screen;
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

@ConflictChain(value = AngelTreads.class, isRoot = true)
public class AngelTreads extends BaseCurio implements ICuriosStorage {
    public AngelTreads(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED, new VariableAttributeModifier(uuid, "angel_treads", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier += 0.1f;
        CalamityHelp.setCalamityFlag(player, 8, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier -= 0.1f;
        CalamityHelp.setCalamityFlag(player, 8, false);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) == 10) {
            var pair = getPair(player);
            float[] count = pair.getA();
            count[0] = 0;
            if (player.isSprinting()) {
                if (count[1] < 0.36f) VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), pair.getB()[0], count[1] += 0.03f);
            } else if (count[1] != 0) {
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), pair.getB()[0], count[1] = 0);
            }

            if (player.getBlockStateOn().is(BlockTags.ICE)) {
                if (count[2] == 0) VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), pair.getB()[0], count[2] = 0.2f);
            } else if (count[2] != 0) {
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), pair.getB()[0], count[2] = 0f);
            }
        }
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("angel_treads", 2));
            tooltips.add(CMLangUtil.getTranslatable("angel_treads", 3));
            tooltips.add(CMLangUtil.getTranslatable("angel_treads", 4));
            tooltips.add(CMLangUtil.getTranslatable("angel_treads", 5));
            tooltips.add(CMLangUtil.getTranslatable("angel_treads", 6));
        } else tooltips.add(CMLangUtil.getTranslatable("angel_treads", 1));
        return tooltips;
    }
}
