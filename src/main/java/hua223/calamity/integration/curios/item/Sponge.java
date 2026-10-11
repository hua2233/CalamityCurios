package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.render.item.IDraedonCreationBG;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

//Refactoring
@CurioRepel(RoverDrive.class)
public class Sponge extends RoverDrive implements IDraedonCreationBG {
    public Sponge(Properties properties) {
        super(properties);
        absorptionRatio = 1.5f;
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(
            uuid, "sponge", 15, AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(), new VariableAttributeModifier(
            uuid, "sponge", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(20)
    public final void damage(DamageListener listener) {
        super.onDamage(listener);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onChangeBarrierAttribute(Player player, GlobalCuriosStorage.CuriosMemory memory, boolean chargingCompleted) {
        UUID uuid = memory.uuids[0];
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), uuid, chargingCompleted ? 15 : 0);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, chargingCompleted ? .1 : 0);
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        super.getSlotsTooltip(tooltips, stack);
        tooltips.add(1, CMLangUtil.getTranslatable("sponge").withStyle(ChatFormatting.YELLOW));
        return tooltips;
    }
}
