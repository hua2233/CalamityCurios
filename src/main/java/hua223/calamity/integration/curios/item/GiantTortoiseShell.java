package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@ConflictChain(GiantShell.class)
public class GiantTortoiseShell extends GiantShell {
    public GiantTortoiseShell(Properties pProperties) {
        super(pProperties);
    }

    protected void handlerAttributes(AttributeInstance speed, VariableAttributeModifier modifier, ServerPlayer player, UUID uuid) {
        modifier.setValue(0, speed);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        VariableAttributeModifier armorMod = VariableAttributeModifier.getModifierInInstance(armor, uuid);
        armorMod.setValue(5, armor);
        AttributeInstance offset = player.getAttribute(Attributes.ARMOR);
        VariableAttributeModifier offsetMod = VariableAttributeModifier.getModifierInInstance(offset, uuid);
        offsetMod.setValue(0, offset);

        DelayRunnable.addRunTask(60, () -> {
            modifier.setValue(-0.1, speed);
            armorMod.setValue(15, armor);
            offsetMod.setValue(0.05, offset);
        });
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "giant_shell", 15, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "giant_shell", -0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "giant_shell", 10, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new VariableAttributeModifier(uuid, "giant_shell", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
