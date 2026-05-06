package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.entity.projectiles.Nebula;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class NebulousCore extends BaseCurio implements ICuriosStorage {
    public NebulousCore(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getMemory(player).putTypeStorage(new ObjectOpenHashSet<UUID>());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        ObjectOpenHashSet<UUID> set = getMemory(player).getTypeStorage(ObjectOpenHashSet.class);
        if (!set.isEmpty()) {
            ServerLevel level = player.serverLevel();
            for (UUID uuid : set) {
                Entity entity = level.getEntity(uuid);
                if (entity != null && entity.isAlive()) entity.discard();
            }
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier("nebulous_core", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        if (memory.count[0]++ >= 20) {
            memory.count[0] = 0;
            ObjectOpenHashSet<UUID> set = memory.getTypeStorage(ObjectOpenHashSet.class);
            ServerLevel level = (ServerLevel) player.level();

            set.removeIf(uuid -> {
                Entity entity = level.getEntity(uuid);
                return entity == null || !entity.isAlive();
            });

            if (set.size() < 10) {
                UUID id = Nebula.spawnAroundPlayer(player);
                if (id != null) set.add(id);
            }
        }
    }

    @ApplyEvent(100)
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            ServerPlayer player = listener.player;
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            listener.canceledEvent();
            player.setHealth(player.getMaxHealth() * 0.4f);
            cooldowns.addCooldown(this, 1800);
        }
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {ObjectOpenHashSet.class};
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.AQUA, "nebulous_core", 1, 2, 3);
        return tooltips;
    }
}
