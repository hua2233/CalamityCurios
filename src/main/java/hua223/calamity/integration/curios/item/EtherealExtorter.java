package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
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
            listener.canceledEvent();
            listener.entity.calamity$HurtNoInvulnerable(DamageSource.OUT_OF_WORLD, listener.baseAmount);
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        float[] flags = getCount(listener.player);
        if (flags[0] == 1) {
            listener.canceledEvent();
            return;
        }

        listener.probability += 0.05f;
        listener.applyAmplifier(0.08f);
        listener.addCallbackAfterCriticalHit(() -> flags[0] = 1);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ethereal_extorter", 1));
        tooltips.add(CMLangUtil.getTranslatable("ethereal_extorter", 2));
        return tooltips;
    }
}
