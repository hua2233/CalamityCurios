package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.EasingType;
import hua223.calamity.util.ItemSupplier;
import io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist.ApothecaristEntity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "药剂师可能出售")
public class Confuse extends Card {
    public Confuse(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            boolean flag = listener.player.Calamity$Player.cardDeck;
            listener.entity.addEffect(new MobEffectInstance(
                CalamityEffects.DECEIVE.get(), flag ? 90 : 60, flag ? 1 : 0));
            listener.player.getCooldowns().addCooldown(this, flag ? 200 : 280);
        }
    }

    @ItemSupplier(ISSMerchant = ApothecaristEntity.class)
    public void onOffers(ApothecaristEntity wizard) {
        RandomSource source = wizard.getRandom();
        if (source.nextFloat() > 0.4f) wizard.getOffers().add(new MerchantOffer(new ItemStack(ItemRegistry.HOGSKIN.get(), 2), new ItemStack(Items.EMERALD,
           10 + (int) (26 * EasingType.POLY_OUT.easingFunction(source.nextFloat(), 2))), getDefaultInstance(), 2, 12, .2f));
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("confuse", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("confuse", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
