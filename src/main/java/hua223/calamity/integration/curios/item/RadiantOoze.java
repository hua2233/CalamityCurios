package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.util.*;
import io.redspace.ironsspellbooks.entity.mobs.wizards.priest.PriestEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "牧师概率交易")
@ConflictChain(AmbrosialAmpoule.class)
public class RadiantOoze extends BaseCurio implements ICuriosStorage {
    public RadiantOoze(Properties properties) {
        super(properties);
    }
    @ItemSupplier(ISSMerchant = PriestEntity.class)
    public void onOffers(PriestEntity wizard) {
        if (wizard.getRandom().nextFloat() < .6f) wizard.getOffers().add(new MerchantOffer(
            new ItemStack(Items.SLIME_BALL, 3), new ItemStack(Items.EMERALD, 30), new ItemStack(this) , 1, 0, .2f));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 20) {
            zeroCount(player, 0);

            float max = player.getMaxHealth();
            float health = player.getHealth();
            int amplifier =  0;

            if (health < max * .2f)  amplifier  = 2;
            else if (health < max * .5f) amplifier  = 1;

            CalamityHelp.addIfDoesNotExist(player, 200, amplifier, MobEffects.REGENERATION);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("radiant_ooze").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
