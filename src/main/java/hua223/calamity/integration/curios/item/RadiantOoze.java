package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(AmbrosialAmpoule.class)
public class RadiantOoze extends BaseCurio implements ICuriosStorage {
    public RadiantOoze(Properties properties) {
        super(properties);
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
