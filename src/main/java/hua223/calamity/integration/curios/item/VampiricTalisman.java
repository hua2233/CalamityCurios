package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitTriggerListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VampiricTalisman extends BaseCurio implements ICuriosStorage {
    public VampiricTalisman(Properties properties) {
        super(properties);
    }

    @ApplyEvent(202)
    public final void onAttack(PlayerAttackListener listener) {
        float[] count = getCount(listener.player);
        if (count[0] > 0 && listener.player.getHealth() < listener.player.getMaxHealth()) {
            listener.player.heal(Math.min(listener.getCorrectionValue(), count[0]));
            count[0] = 0;
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.applyAmplifier(0.12f);
        getCount(listener.player)[0] = listener.target.getHealth();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("vampiric_talisman", 1));
        tooltips.add(CMLangUtil.getTranslatable("vampiric_talisman", 2));
        return tooltips;
    }
}
