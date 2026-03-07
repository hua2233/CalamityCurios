package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VampiricTalisman extends BaseCurio {
    public VampiricTalisman(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.applyAmplifier(0.12f);
        listener.addCallbackAfterCriticalHit(() -> {
            final float currentHeal = listener.target.getHealth();
            DelayRunnable.currentTickEndRun(() -> {
                float heal;
                //Unless multiple players simultaneously bypass the invincible frame and attack the target in this Tick,
                //this is generally unlikely
                if (listener.target.isDeadOrDying()) heal = currentHeal;
                else heal = currentHeal - listener.target.getHealth();

                if (heal > 0 && listener.player.getHealth() < listener.player.getMaxHealth())
                    listener.player.heal(Math.round(heal * 0.25f));
            });
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("vampiric_talisman", 1));
        tooltips.add(CMLangUtil.getTranslatable("vampiric_talisman", 2));
        return tooltips;
    }
}
