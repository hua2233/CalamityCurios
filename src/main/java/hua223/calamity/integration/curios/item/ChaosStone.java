package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ChaosStone extends BaseCurio {
    public ChaosStone(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (listener.effect == CalamityEffects.MANA_SICKNESS.get()) {
            listener.canceledEvent();
            MobEffect effect = CalamityEffects.MANA_BURN.get();
            int duration = listener.instance.getDuration();
            if (listener.player.hasEffect(effect)) duration += listener.player.getEffect(effect).getDuration();

            listener.player.addEffect(new MobEffectInstance(effect, duration));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("chaos_stone", 2));
            tooltips.add(CMLangUtil.getTranslatable("chaos_stone", 3));
            tooltips.add(CMLangUtil.getTranslatable("chaos_stone", 4));
        } else tooltips.add(CMLangUtil.getTranslatable("chaos_stone", 1));
        return tooltips;
    }
}
