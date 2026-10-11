package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@CurioRepel(Absorber.class)
public class GrandGelatin extends VitalJelly {
    public GrandGelatin(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(DamageListener listener) {
        ServerPlayer player = listener.player;
        if (player.getHealth() < player.getMaxHealth() / 2) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
            removeAllDeBuff(player);
            cooldowns.addCooldown(this, 300);
        }
    }

    public static void removeAllDeBuff(ServerPlayer player) {
        player.getActiveEffects().removeIf(instance -> !instance.getEffect().isBeneficial());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("grand_gelatin").withStyle(ChatFormatting.GREEN));
        return tooltips;
    }
}
