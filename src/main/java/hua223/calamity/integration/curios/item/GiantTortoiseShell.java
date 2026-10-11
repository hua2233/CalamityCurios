package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CurioRepel;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@CurioRepel(Absorber.class)
public class GiantTortoiseShell extends GiantShell {
    public GiantTortoiseShell(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void damage(DamageListener listener) {
        super.onDamage(listener);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void handleAttributes(Player player, GlobalCuriosStorage.CuriosMemory memory, boolean remove) {
        if (remove) memory.count[0] = 60;
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), memory.uuids[0], remove ? 5 : 15);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], remove ? 0 : -.1);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), memory.uuids[0], remove ? 0 : .05);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
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
