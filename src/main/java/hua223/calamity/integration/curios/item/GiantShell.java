package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@CurioRepel(GiantTortoiseShell.class)
@JeiInfo(zh_cn = "击杀成年海龟可以概率掉落")
public class GiantShell extends BaseCurio implements ICuriosStorage {
    public GiantShell(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "giant_shell", 6, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "giant_shell", -0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(220)
    @SuppressWarnings("ConstantConditions")
    public final void onDamage(DamageListener listener) {
        ServerPlayer player = listener.player;
        GlobalCuriosStorage.CuriosMemory memory = getMemory(player);
        if (memory.count[0] == 0) handleAttributes(player, memory, true);
    }

    @SuppressWarnings("ConstantConditions")
    protected void handleAttributes(Player player, GlobalCuriosStorage.CuriosMemory memory, boolean remove) {
        if (remove) memory.count[0] = 60;

        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), memory.uuids[0], remove ? 3 : 6);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.MOVEMENT_SPEED), memory.uuids[0], remove ? 0 : -.1);
    }

    @ApplyGlobalLoot
    public void onEntity(EntitiesLootContext context) {
        Turtle turtle = context.verification(EntityType.TURTLE);
        if (turtle != null && !turtle.isBaby() && context.chance(.18f))
            context.addLoot(this);
    }

    @Override
    protected void onPlayerTick(Player player) {
        GlobalCuriosStorage.CuriosMemory memory = getMemory(player);
        float[] count = memory.count;
        if (count[0] > 0 && --count[0] == 0) handleAttributes(player, memory, false);
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
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("giant_shell"));
        return tooltips;
    }
}
