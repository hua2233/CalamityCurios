package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            boolean flag = CalamityHelp.getCalamityFlag(listener.player, 10);
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.VOID_TOUCH.get(),
                flag ? 100 : 60, flag ? 1 : 0));
            listener.player.getCooldowns().addCooldown(this, flag ? 160 : 200);
        }
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nothing").withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
