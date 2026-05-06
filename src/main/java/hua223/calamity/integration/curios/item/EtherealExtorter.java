package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitCheckListener;
import hua223.calamity.integration.curios.listeners.CriticalHitTriggerListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class EtherealExtorter extends BaseCurio implements ICuriosStorage {
    public EtherealExtorter(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        float[] flags = getCount(listener.player);
        if (flags[0] == 1) {
            flags[0] = 0;
            listener.setSource(listener.player.damageSources().fellOutOfWorld());
        }
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        listener.probability += 0.05f;
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.applyAmplifier(0.08f);
        getCount(listener.player)[0] = 1;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "ethereal_extorter", 1, 2);
        return tooltips;
    }
}
