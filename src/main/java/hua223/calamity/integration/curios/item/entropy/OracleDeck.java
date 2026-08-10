package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.Decks;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.items.UnsealingRope;
import hua223.calamity.register.attribute.CalamityAttributes;
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

public class OracleDeck extends Decks implements ICuriosStorage {
    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        super.setAttributeModifiers(uuid, stack, modifier, equipped);
        if (isUnsealing(stack)) {
            modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "oracle_deck", 20, AttributeModifier.Operation.ADDITION));
            modifier.put(CalamityAttributes.ARMOR_PENETRATE.get(),
                new AttributeModifier(uuid, "oracle_deck", 5, AttributeModifier.Operation.ADDITION));
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        float max = listener.player.getMaxHealth() / 2;
        if (listener.baseAmount > max) listener.setFinalAmount(max);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 120)) {
            List<Player> players = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(7));
            for (Player player1 : players)
                if (player1.getHealth() < player1.getMaxHealth())
                    player1.heal(4f);
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "oracle_deck", 1, 2, 3);
        return tooltips;
    }
}
