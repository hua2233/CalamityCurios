package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.PotionCloudThrownItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class InkBomb extends BaseCurio {
    public InkBomb(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && !listener.player.getCooldowns().isOnCooldown(this)) {
            ItemStack stack = getDefaultInstance();
            float yRot = listener.player.getYRot();
            for (int i = 0; i < 4; i++) {
                PotionCloudThrownItem potion = new PotionCloudThrownItem(listener.player,
                    0xFF2B1B17, new MobEffectInstance(CalamityEffects.CONFUSED.get(), 80));
                potion.setItem(stack);
                potion.setOwner(listener.player);
                potion.shootFromRotation(listener.player, 0, yRot + i * 90, -20f, 0.5f, 1f);
                listener.player.level().addFreshEntity(potion);
            }

            listener.player.heal(4f);
            listener.player.getCooldowns().addCooldown(this, 500);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "ink_bomb", 1, 2, 3);
        return tooltips;
    }
}
