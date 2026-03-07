package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class CoinOfDeceit extends BaseCurio {
    public CoinOfDeceit(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        //This is the 100th curio, so as a commemoration implant a small easter egg
        listener.addCallbackAfterCriticalHit(() -> {
            float probability = listener.player.getRandom().nextFloat();
            listener.applyAmplifier(probability < 0.4f ? probability < 0.001f ? 100f : 0.3f : 0);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("coin_of_deceit", 1));
        tooltips.add(CMLangUtil.getTranslatable("coin_of_deceit", 2));
        return tooltips;
    }
}
