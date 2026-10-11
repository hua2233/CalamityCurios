package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.particle.ColorfulTotemType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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


    public boolean canceledPlayerDeathIfNotCooldowns(Item item, float recovery, int cooldownTime) {
        if (isPlayerDeath) {
            ItemCooldowns instance = player.getCooldowns();
            if (!instance.isOnCooldown(item)) {
                canceledEvent();
                player.removeAllEffects();
                player.setHealth(player.getMaxHealth() * recovery);
                if (cooldownTime > 0) instance.addCooldown(item, cooldownTime);
                return true;
            }
        }

        return false;
    }

    public boolean canceledPlayerDeathIfNotCooldowns(Item item, float recovery, int cooldownTime, int... colors) {
        if (canceledPlayerDeathIfNotCooldowns(item, recovery, cooldownTime)) {
            sendAnimatePack(item, player, (IDataPackResponse) CalamityItems.NEBULOUS_CORE.get(), colors);
            return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public static void sendAnimatePack(Item item, ServerPlayer player, IDataPackResponse response, int... colors) {
        CompoundTag tag = response.getPack();
        int[] i = new int[colors.length + 2];
        i[0] = player.getId();
        i[1] = item == null ? -1 : BuiltInRegistries.ITEM.getId(item);
        System.arraycopy(colors, 0, i, 2, colors.length);
        tag.putIntArray("totem", i);
        response.sendToTrackingAndSelf(player);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnParticle(Minecraft minecraft, Entity entity, int[] info) {
        int[] colors = new int[info.length - 2];
        ParticleOptions data;
        if (colors.length != 0) {
            System.arraycopy(info, 2, colors, 0, colors.length);
            data = new ColorfulTotemType.ColorfulTotemOptions(colors);
        } else data = ParticleTypes.TOTEM_OF_UNDYING;

        minecraft.particleEngine.createTrackingEmitter(entity, data, 30);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void baseAnimation(CompoundTag tag) {
        Minecraft minecraft = Minecraft.getInstance();
        int[] totem = tag.getIntArray("totem");
        ClientLevel level = minecraft.level;
        Entity entity = level.getEntity(totem[0]);
        if (entity != null) {
            spawnParticle(minecraft, entity, totem);
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
            if (entity == minecraft.player && totem[1] != -1)
                minecraft.gameRenderer.displayItemActivation(BuiltInRegistries.ITEM.byId(totem[1]).getDefaultInstance());
        }
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
