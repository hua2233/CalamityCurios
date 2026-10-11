package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.generators.VillagerProfessionMap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.ItemSupplier;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "由村庄牧师交易，声望越高概率越大")
public class Brilliance extends Card implements ICuriosStorage {
    public Brilliance(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] == 5) {
            count[0] = 0;
            CalamityLightBlock.placePlayerDynamicLightSource(player,
                player.Calamity$Player.cardDeck ? 15 : 8);
        }
    }

    @ItemSupplier(villagerSupplier = VillagerProfessionMap.CLERIC, villagerLevel = 2)
    public MerchantOffer villagerTransaction(Entity entity, RandomSource source) {
        Villager villager = (Villager) entity;
        Player player = villager.getTradingPlayer();
        float probability = .2f + (player == null ? 0 : villager.getPlayerReputation(player)) / 100f;
        return source.nextFloat() < probability ? null : new MerchantOffer(new ItemStack(Items.PAPER, 10),
            new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 3), getDefaultInstance(), 2, 10, .2f);
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
        tooltips.add(CMLangUtil.getTranslatable("brilliance", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("brilliance", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
