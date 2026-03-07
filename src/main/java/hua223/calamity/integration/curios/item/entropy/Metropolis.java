package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Metropolis extends Card {
    public Metropolis(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 2, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 2, false);
    }

    @ApplyGlobalLoot
    public final void onGlobalChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("ancient_city_ice_box") && context.chance(0.75f))
            context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("metropolis", 1));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("metropolis", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
