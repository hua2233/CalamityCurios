package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ItemSupplier;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerEntity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "炽焰术士可能出售")
public class Tarnish extends Card {
    public Tarnish(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        boolean flag = listener.player.Calamity$Player.cardDeck;
        if (listener.entity.wasOnFire) listener.amplifier += flag ? .01f : 0.06f;
        else listener.entity.setSecondsOnFire(flag ? 8 : 4);
    }

    @ItemSupplier(ISSMerchant = PyromancerEntity.class)
    public void onOffers(PyromancerEntity entity) {
        RandomSource source = entity.getRandom();
        if (source.nextFloat() > .45f) {
            //drunkard
            entity.getOffers().add(new MerchantOffer(new ItemStack(ItemRegistry.FIRE_ALE.get(), 4), new ItemStack(Items.EMERALD,
                source.nextInt(12, 21)), new ItemStack(this), 0, 2, 22, .25f));
        }
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("tarnish", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("tarnish", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
