package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class VexationNecklace extends BaseCurio implements ICuriosStorage {
    public VexationNecklace(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        CalamityHelp.addIfDoesNotExist(listener.entity, 80, 0, CalamityEffects.ACID_VENOM.get());
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new VariableAttributeModifier(uuid, "rotten", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 20) {
            var memory = getMemory(player);
            float[] count = memory.count;
            count[0] = 0;
            if (player.getHealth() >= player.getMaxHealth() / 2) {
                if (count[1] == 0) {
                    VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), memory.uuids[0], 0.2);
                    count[1] = 1;
                }
            } else if (count[1] == 1){
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), memory.uuids[0], 0);
                count[1] = 0;
            }
        }
    }

    @Override
    @ApplyEvent
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("vexation"));
        return tooltips;
    }
}
