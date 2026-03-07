package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ThrownItemPotion;
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
                ThrownItemPotion potion = new ThrownItemPotion(listener.player, this,
                    new MobEffectInstance(CalamityEffects.CONFUSED.get(), 80));
                potion.setLingering(0xFF2B1B17);
                potion.setItem(stack);
                potion.setOwner(listener.player);
                potion.shootFromRotation(listener.player, 0, yRot + i * 90, -20f, 0.5f, 1f);
                listener.player.level.addFreshEntity(potion);
            }

            listener.player.getCooldowns().addCooldown(this, 500);
            if (listener.player.getHealth() < listener.player.getMaxHealth())
                listener.player.heal(4f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ink_bomb", 1));
        tooltips.add(CMLangUtil.getTranslatable("ink_bomb", 2));
        tooltips.add(CMLangUtil.getTranslatable("ink_bomb", 3));
        return tooltips;
    }
}
