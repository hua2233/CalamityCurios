package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
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

        if (listener.isTriggerByLiving) {
            for (int i = 0; i < 3; i++) {
                Meteor.of(listener.entity, player, false);
            }
        }

        float percentageDifference = player.getHealth() / player.getMaxHealth();
        if (percentageDifference <= 0.25f) {
            player.invulnerableTime += 40;
        } else {
            int quotient = (int) (percentageDifference / 0.05f) + 10;
            player.invulnerableTime += quotient;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("deific_amulet", 1));
        tooltips.add(CMLangUtil.getTranslatable("deific_amulet", 2));
        tooltips.add(CMLangUtil.getTranslatable("deific_amulet", 3));
        return tooltips;
    }
}
