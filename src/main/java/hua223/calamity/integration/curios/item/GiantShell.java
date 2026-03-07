package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = GiantShell.class, isRoot = true)
public class GiantShell extends BaseCurio implements ICuriosStorage {
    public GiantShell(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "giant_shell", 6, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "giant_shell", -0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        UUID uuid = getFirstUUID(player);
        VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(speed, uuid);
        if (speed.getValue() < 0) handlerAttributes(speed, modifier, player, uuid);
    }

    protected void handlerAttributes(AttributeInstance speed, VariableAttributeModifier modifier, ServerPlayer player, UUID uuid) {
        modifier.setValue(0, speed);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        VariableAttributeModifier armorMod = VariableAttributeModifier.getModifierInInstance(armor, uuid);
        armorMod.setValue(3, armor);

        DelayRunnable.addRunTask(60, () -> {
            modifier.setValue(-0.1, speed);
            armorMod.setValue(6, armor);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("giant_shell"));
        return tooltips;
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
    public boolean storageCount() {
        return false;
    }
}
