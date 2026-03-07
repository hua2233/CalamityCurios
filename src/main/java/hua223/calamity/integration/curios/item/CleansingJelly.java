package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;

public class CleansingJelly extends BaseCurio {
    public CleansingJelly(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) return;
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) return;
        List<MobEffectInstance> harmful = effects.stream().filter(effect -> !effect.getEffect().isBeneficial()).toList();
        if (harmful.size() < 2) return;
        for (MobEffectInstance instance : harmful) {
            player.removeEffect(instance.getEffect());
        }
        cooldowns.addCooldown(this, 600);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("cleansing_jelly"));
        return tooltips;
    }
}
