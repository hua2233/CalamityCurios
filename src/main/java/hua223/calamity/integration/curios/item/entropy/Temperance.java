package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.PlayerServantsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Temperance extends Card {
    public Temperance(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        boolean deck = equipFromDeck(stack);
        PlayerServantsManager.loadPlayerServantsEntity(player, servants -> {
            PlayerServantsManager.changeAttribute(servants, Attributes.MAX_HEALTH, deck ? 1 : 0.6, AttributeModifier.Operation.MULTIPLY_BASE);
            PlayerServantsManager.changeAttribute(servants, Attributes.ATTACK_DAMAGE, deck ? 0.5 : 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        });
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        boolean deck = equipFromDeck(stack);
        PlayerServantsManager.removePlayerServantsEntity(player, servants -> {
            PlayerServantsManager.changeAttribute(servants, Attributes.MAX_HEALTH, deck ? -1 : -0.6, AttributeModifier.Operation.MULTIPLY_BASE);
            PlayerServantsManager.changeAttribute(servants, Attributes.ATTACK_DAMAGE, deck ? -0.5 : -0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        });
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.chance(0.4f) && context.entity instanceof OwnableEntity entity
            && entity.getOwner() == context.player) context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("temperance").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
