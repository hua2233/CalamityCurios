package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ChaosStone extends BaseCurio {
    public ChaosStone(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (listener.effect == CalamityEffects.MANA_SICKNESS.get()) {
            listener.canceledEvent();
            MobEffect effect = CalamityEffects.MANA_BURN.get();
            int duration = listener.instance.getDuration();
            MobEffectInstance instance = listener.player.getEffect(effect);
            if (instance != null) listener.setEffectDuration(instance.getDuration() + duration);
            else listener.player.addEffect(new MobEffectInstance(effect, duration));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "chaos_stone", 2, 3, 4);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("chaos_stone", 1).withStyle(ChatFormatting.RED));
        return tooltips;
    }
}
