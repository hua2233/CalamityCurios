package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class BloodflareCore extends BaseCurio implements ICuriosStorage {
    public BloodflareCore(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            AttributeInstance instance = listener.player.getAttribute(Attributes.ARMOR);

            UUID uuid = getUUID(listener.player)[0];
            VariableAttributeModifier.updateModifierInInstance(instance, uuid, 0);
            listener.player.getPersistentData().putDouble("maxArmor", instance.getValue());

            VariableAttributeModifier.updateModifierInInstance(instance, uuid, -0.5);
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level.isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(
            uuid, "bloodflare", 0, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.getPersistentData().putDouble("maxArmor", 0);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.getPersistentData().remove("maxArmor");
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 20) {
            var p = getPair(player);
            p.getA()[0] = 0;
            AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
            double value = instance.getModifier(p.getB()[0]).getAmount();

            if ((value += 0.1) > 0) return;
            CompoundTag tag = player.getPersistentData();

            double max = tag.getDouble("maxArmor");

            VariableAttributeModifier.updateModifierInInstance(instance, p.getB()[0], value);

            player.heal((float) (max - instance.getValue()));
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("bloodflare_core", 1));
        tooltips.add(CMLangUtil.getTranslatable("bloodflare_core", 2));
        tooltips.add(CMLangUtil.getTranslatable("bloodflare_core", 3));
        return tooltips;
    }
}