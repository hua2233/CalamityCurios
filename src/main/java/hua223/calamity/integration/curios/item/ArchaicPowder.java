package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.ChangedDimensionListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class ArchaicPowder extends BaseCurio implements ICuriosStorage {
    public ArchaicPowder(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onDimensionChange(ChangedDimensionListener listener) {
        ServerPlayer player = listener.player;
        AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
        var memory = getMemory(player);
        UUID uuid = memory.uuids[0];
        float[] count = memory.count;
        if (listener.to == Level.NETHER) {
            VariableAttributeModifier.updateModifierInInstance(instance, uuid, 6);
            VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, 0.05);
            count[0] = 0;
            count[1] = 1;
        } else if (listener.from == Level.NETHER) {
            if (player.getY() >= 10) {
                VariableAttributeModifier.updateModifierInInstance(instance, uuid, 0);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, 0);
                count[2] = 0;
            } else count[2] = 1;

            count[1] = 0;
            count[0] = 0;
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (player.level().dimension() == Level.NETHER)
            getCount(player)[1] = 1;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        boolean is = equipped.level().dimension() == Level.NETHER;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "archaic_powder",  is ? 6 : 0, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new VariableAttributeModifier(uuid, "archaic_powder", is ? 0.05 : 0, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(uuid, "archaic_powder", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
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
    protected void onPlayerTick(Player player) {
        GlobalCuriosStorage.CuriosMemory memory = getMemory(player);
        if (memory.count[1] == 0 && memory.count[0]++ > 20) {
            memory.count[0] = 0;
            AttributeInstance instance = player.getAttribute(Attributes.ARMOR);

            if (player.getY() < -10) {
                if (memory.count[2] == 0) setAttribute(player, instance, memory, 6, 0.05, 1);
            } else if (memory.count[2] == 1)
                setAttribute(player, instance, memory, 0, 0, 0);
        }
    }

    private static void setAttribute(Player player, AttributeInstance instance, GlobalCuriosStorage.CuriosMemory memory, double armor, double offset, float flag) {
        UUID uuid = memory.uuids[0];
        VariableAttributeModifier.updateModifierInInstance(instance, uuid, armor);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, offset);
        memory.count[2] = flag;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("archaic_powder").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
