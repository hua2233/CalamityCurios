package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Nothing extends Card {
    public Nothing(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        MobEffect effect = CalamityEffects.VOID_TOUCH.get();
        MobEffectInstance instance = listener.entity.getEffect(effect);
        if (instance != null) {
            boolean flag = CalamityHelp.getCalamityFlag(listener.player, 10);
            int amplifier = instance.getAmplifier();
            if (amplifier < 2 || (flag && amplifier < 4))
                listener.entity.addEffect(new MobEffectInstance(effect, flag ? 100 : 60, amplifier + 1));
        }
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nothing", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("nothing", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
