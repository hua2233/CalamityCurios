package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Brilliance extends Card implements ICuriosStorage {
    public Brilliance(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] == 5) {
            count[0] = 0;
            CalamityLightBlock.placePlayerDynamicLightSource(player,
                CalamityHelp.getCalamityFlag(player, 10) ? 15 : 8);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WANDERING_TRADER)
            && context.player.level.isDay() && context.chance(0.2f))
            context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        //群星与我们也同样如此
        tooltips.add(CMLangUtil.getTranslatable("brilliance").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
