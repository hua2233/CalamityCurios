package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.PlayerHealListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
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
    public Regenator(Properties properties) {
        super(properties);
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
            new AttributeModifier(uuid, "regenator", -0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "regenator", 4, AttributeModifier.Operation.ADDITION));
        
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @ApplyEvent(2000)
    public final void onDamage(DamageListener listener) {
        listener.player.calamity$SetInvulnerableTime((int) (listener.player.invulnerableTime * 1.4f));
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        listener.floating += getCount(listener.player)[2];
    }

    @ApplyEvent(120)
    public final void onHeal(PlayerHealListener listener) {
        float[] count = getCount(listener.player);
        if (count[1] != 1) {
            count[2] += listener.getCorrectionValue();
            listener.canceledEvent();
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] == 20) {
            count[0] = 0;
            count[1] = 1;
            player.heal(player.getMaxHealth() * 0.075f);
            count[1] = 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "regenator", 1, 2, 3);
        return tooltips;
    }
}
