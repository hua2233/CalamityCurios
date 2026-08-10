package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.net.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Metropolis extends Card implements IDataPackResponse {
    public Metropolis(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getPack().putBoolean("flag", true);
        sendToAllClient();
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        getPack().putBoolean("flag", false);
        sendToAllClient();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().sneakingSpeedBonus = tag.getBoolean("flag");
    }

    @ApplyGlobalLoot
    public void onGlobalChestLoot(ChestLootContext context) {
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
        tooltips.add(CMLangUtil.getTranslatable("metropolis", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("metropolis", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
