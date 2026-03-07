package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class RadiantOoze extends BaseCurio implements ICuriosStorage {
    public RadiantOoze(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 10) {
            zeroCount(player, 0);

            float max = player.getMaxHealth();
            float health = player.getHealth();

            if (health < max * .2f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));

            } else if (health < max * .5f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

            } else if (health < max * .7f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("radiant_ooze"));
        return tooltips;
    }
}
