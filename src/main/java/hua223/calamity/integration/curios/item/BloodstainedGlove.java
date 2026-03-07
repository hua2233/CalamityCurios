package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = BloodstainedGlove.class, isRoot = true)
public class BloodstainedGlove extends BaseCurio {
    public BloodstainedGlove(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.addCallbackAfterCriticalHit(() -> {
            listener.addSinglePenetration(8f);
            if (listener.player.getHealth() < listener.player.getMaxHealth())
                listener.player.heal(2f);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("bloodstained_glove"));
        return tooltips;
    }
}
