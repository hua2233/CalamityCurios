package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.items.CalamityItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class DeathListener extends BaseListener<LivingDeathEvent> {
    public final DamageSource source;
    public final LivingEntity entity;
    public final boolean isPlayerDeath;
    public ServerPlayer player;

    @EventConstructor
    public DeathListener(LivingDeathEvent event, ServerPlayer player, Boolean isPlayerDeath) {
        super(event);
        source = event.getSource();
        this.isPlayerDeath = isPlayerDeath;
        if (isPlayerDeath) {
            this.player = player;
            if (event.getSource().getEntity() instanceof LivingEntity e) {
                entity = e;
            } else entity = null;
        } else {
            entity = event.getEntity();
            this.player = player;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean canceledPlayerDeathIfNotCooldowns(Item item, float recovery, int cooldownTime, int... colors) {
        if (isPlayerDeath) {
            ItemCooldowns instance = player.getCooldowns();
            if (!instance.isOnCooldown(item)) {
                canceledEvent();
                player.removeAllEffects();
                player.setHealth(player.getMaxHealth() * recovery);

                instance.addCooldown(item, cooldownTime);
                IDataPackResponse response = (IDataPackResponse) CalamityItems.NEBULOUS_CORE.get();
                CompoundTag tag = response.getPack();
                int[] i = new int[colors.length + 2];
                i[0] = player.getId();
                i[1] = BuiltInRegistries.ITEM.getId(item);
                System.arraycopy(colors, 0, i, 2, colors.length);
                tag.putIntArray("totem", i);
                response.sendToAllClient();
                return true;
            }
        }

        return false;
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
