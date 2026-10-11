package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CurioRepel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@CurioRepel(Abaddon.class)
public class Apollyon extends Abaddon {
    public Apollyon(Properties properties) {
        super(properties);
        criticalStrikeAmplificationFactor = 8f;
        maximumPropagationNumber = 10;
    }

    @ApplyEvent
    public final void attack(PostAttackListener listener) {
        if (listener.isCalamityCriticalHits) {
            if (!listener.entity.getActiveEffects().isEmpty())
                listener.amplifier += CalamityHelp.getDebuffCount(listener.entity) * 0.03f;
            super.onAttack(listener);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void displayDpsInformation(List<Component> tooltips, Style style) {
        tooltips.add(CMLangUtil.getTranslatable("apollyon").setStyle(style));
        super.displayDpsInformation(tooltips, style);
    }
}
