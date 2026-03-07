package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
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

public class Regenator extends BaseCurio implements ICuriosStorage {
    public Regenator(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "regenator", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "regenator", 4, AttributeModifier.Operation.ADDITION));
        
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 20) {
            zeroCount(player, 0);
            double max = player.getMaxHealth();
            if (max == player.getHealth()) return;
            player.heal((float) (max * 0.2));
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("regenator"));
        return tooltips;
    }
}
