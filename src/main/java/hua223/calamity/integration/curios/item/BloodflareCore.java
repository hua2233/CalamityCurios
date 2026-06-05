package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
    public BloodflareCore(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            AttributeInstance instance = listener.player.getAttribute(Attributes.ARMOR);
            var memory = getMemory(listener.player);
            VariableAttributeModifier.updateModifierInInstance(instance, memory.uuids[0], 0);
            float amount  = (float) instance.getValue();
            if (amount <= 1) return;

            //Maximum damage
            memory.count[1] = amount / 2;
            //Recovery per second
            memory.count[2] = Math.max(1, memory.count[1] / 5);
            VariableAttributeModifier.updateModifierInInstance(instance, memory.uuids[0], -memory.count[1]);
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(
            uuid, "blood_flare_core", 0, AttributeModifier.Operation.ADDITION));
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
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        var p = getMemory(player);
        if (p.count[0]++ >= 20) {
            p.count[0] = 0;
            if (p.count[1] != 0) {
                AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
                float value = (float) (instance.getModifier(p.uuids[0]).getAmount() + p.count[2]);
                if (value >= p.count[1]) {
                    value = p.count[1];
                    p.count[1] = 0;
                }
                player.heal(p.count[2]);
                VariableAttributeModifier.updateModifierInInstance(instance, p.uuids[0], value);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "blood_flare_core", 1, 2, 3);
        return tooltips;
    }
}