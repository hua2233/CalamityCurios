package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FrostBarrier extends BaseCurio {
    public FrostBarrier(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (!listener.isTriggerByLiving) return;
        if (listener.entity instanceof Mob attack) {
            float amplifier = listener.baseAmount / listener.player.getMaxHealth();
            if (amplifier > 1) amplifier = 1;
            attack.addEffect(new MobEffectInstance(CalamityEffects.GLACIAL_STATE.get(), (int) (amplifier * 300), 0));
        }

        if (listener.source == DamageSource.FREEZE) listener.canceledEvent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("frost_barrier"));
        return tooltips;
    }
}
