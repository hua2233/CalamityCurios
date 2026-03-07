package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class LifeJelly extends BaseCurio {
    public LifeJelly(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (player.getHealth() < player.getMaxHealth() / 2) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            cooldowns.addCooldown(this, 600);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("life_jelly"));
        return tooltips;
    }
}
