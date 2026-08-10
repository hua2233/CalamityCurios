package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@CurioRepel(RuinMedallion.class)
public class CoinOfDeceit extends BaseCurio {
    public CoinOfDeceit(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitCheckListener listener) {
        listener.applyAmplifier(0.06f);
        listener.probability += 0.06f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("coin_of_deceit"));
        return tooltips;
    }
}
