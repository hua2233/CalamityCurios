package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
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
    public final void onAttack(PlayerAttackListener listener) {
        ServerPlayer player = listener.player;
        if (player.getMaxHealth() == player.getHealth()) {
            double value = listener.player.getAttributeValue(CalamityAttributes.INJURY_OFFSET.get()) - 1;
            if (value <= 0) return;
            listener.amplifier += (float) value / 2;
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (player.getMaxHealth() == player.getHealth()) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            listener.amplifier -= 0.5f;
            cooldowns.addCooldown(this, 200);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("bee", 1));
        tooltips.add(CMLangUtil.getTranslatable("bee", 2));
        return tooltips;
    }
}
