package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Barren extends Card {
    public Barren(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            WitherSkull skull = EntityType.WITHER_SKULL.create(listener.projectile.level);
            if (skull != null) {
                skull.setPos(listener.player.getX(), listener.player.getEyeY() - 0.1, listener.player.getZ());
                skull.setOwner(listener.player);
                skull.shootFromRotation(listener.player, listener.player.getXRot(), listener.player.getYRot(),
                    0.0F, 2.0F, 1.0F);
                if (listener.player.level.addFreshEntity(skull)) listener.player.getCooldowns().addCooldown(
                    this, CalamityHelp.getCalamityFlag(listener.player, 10) ? 120 : 200);
            }
        }
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("barren", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("barren", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
