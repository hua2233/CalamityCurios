package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.ProjectileSpawnListener;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiInfo(zh_cn = "击杀凋零骷髅时小概率掉落，受抢夺影响")
public class Barren extends Card {
    public Barren(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            WitherSkull skull = EntityType.WITHER_SKULL.create(listener.projectile.level());
            if (skull != null) {
                skull.setPos(listener.player.getX(), listener.player.getEyeY() - 0.1, listener.player.getZ());
                skull.setOwner(listener.player);
                skull.shootFromRotation(listener.player, listener.player.getXRot(), listener.player.getYRot(),
                    0.0F, 2.0F, 1.0F);
                if (listener.player.level().addFreshEntity(skull)) listener.player.getCooldowns().addCooldown(
                    this, listener.player.Calamity$Player.cardDeck ? 80 : 160);
            }
        }
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WITHER_SKELETON)
            && context.lootingChance(.025f)) context.addLoot(this);
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
