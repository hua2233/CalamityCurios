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
import net.minecraft.world.entity.LivingEntity;
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
        final boolean deck = equipFromDeck(stack);
        PlayerServantsManager.loadPlayerServantsEntity(player, servant -> modifyServantAttribute(servant, deck));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        final boolean deck = equipFromDeck(stack);
        PlayerServantsManager.removePlayerServantsEntity(player, servant -> modifyServantAttribute(servant, deck));
    }

    private void modifyServantAttribute(LivingEntity servant, boolean deck) {
        PlayerServantsManager.changeAttribute(servant, Attributes.MAX_HEALTH, deck ? 1 : 0.6, AttributeModifier.Operation.MULTIPLY_BASE);
        PlayerServantsManager.changeAttribute(servant, Attributes.ARMOR, deck ? 8 : 4, AttributeModifier.Operation.ADDITION);
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
        tooltips.add(CMLangUtil.getTranslatable("temperance", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("temperance", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
