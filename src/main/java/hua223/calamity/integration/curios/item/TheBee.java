package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class TheBee extends BaseCurio {
    public TheBee(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        if (listener.player.getMaxHealth() == listener.player.getHealth())
            listener.amplifier += (float) listener.player.getAttributeValue(CalamityAttributes.INJURY_OFFSET.get()) - 1;
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
        ServerPlayer player = listener.player;
        if (player.getMaxHealth() == player.getHealth()) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            listener.amplifier -= 0.5f;
            cooldowns.addCooldown(this, 140);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "bee", 1, 2);
        return tooltips;
    }
}
