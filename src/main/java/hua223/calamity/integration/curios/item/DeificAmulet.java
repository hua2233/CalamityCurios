package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DeificAmulet extends BaseCurio {
    public DeificAmulet(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;

        if (listener.isTriggerByLiving && !player.getCooldowns().isOnCooldown(this)) {
            Meteor.of(listener.entity, player, false);
            player.getCooldowns().addCooldown(this, 200);
        }

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (health / maxHealth <= 0.25f) player.calamity$SetInvulnerableTime(40);
        else player.calamity$SetInvulnerableTime((int) (((1 - health / (maxHealth * 0.75f)) * 30f) + 10));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "deific_amulet", 1, 2, 3);
        return tooltips;
    }
}
