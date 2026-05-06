package hua223.calamity.util;

import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerServantsManager {
    private static Map<UUID, Pair<short[], Set<LivingEntity>>> servantsSet;
    private static boolean removeFromMap;

    private PlayerServantsManager() {}

    public static void loadPlayerServantsEntity(ServerPlayer player, Consumer<LivingEntity> onJoin) {
        Set<LivingEntity> set;
        if (servantsSet == null) {
            servantsSet = new Object2ObjectOpenHashMap<>();
            servantsSet.put(player.getUUID(), new Pair<>(new short[] {1}, set = new ObjectOpenHashSet<>()));
            MinecraftServer server = player.server;
            DelayRunnable.conditionsLoop(() -> {
                if (servantsSet.isEmpty()) {
                    servantsSet = null;
                    return true;
                }

                for (Pair<short[], Set<LivingEntity>> entry : servantsSet.values()) {
                    Set<LivingEntity> entities = entry.getB();
                    if (entities.isEmpty()) continue;

                    entities.removeIf(LivingEntity::isDeadOrDying);
                }

                PlayerList playerList = server.getPlayerList();
                //Uninstall deceased old players
                for (UUID uuid : servantsSet.keySet()) {
                    ServerPlayer owner = playerList.getPlayer(uuid);
                    if (owner == null || owner.isDeadOrDying()) {
                        servantsSet.remove(uuid);
                        if (servantsSet.isEmpty()) {
                            servantsSet = null;
                            return true;
                        }
                    }
                }

                return false;
            }, 60);
        } else if (servantsSet.containsKey(player.getUUID())) {
            Pair<short[], Set<LivingEntity>> pair = servantsSet.get(player.getUUID());
            pair.getA()[0]++;
            set = pair.getB();
        } else {
            servantsSet.put(player.getUUID(), new Pair<>(new short[] {1}, set = new ObjectOpenHashSet<>()));
        }

        for (LivingEntity entity : loadLevelServantsEntity(player)) {
            set.add(entity);
            if (onJoin != null) onJoin.accept(entity);
        }
    }

    public static List<? extends LivingEntity> loadLevelServantsEntity(ServerPlayer player) {
        return player.serverLevel().getEntities(EntityTypeTest.forClass(LivingEntity.class), possibleEntity ->
            possibleEntity.isAlive() && possibleEntity instanceof OwnableEntity entity && entity.getOwner() == player);
    }

    public static void removePlayerServantsEntity(ServerPlayer player, Consumer<LivingEntity> onRemove) {
        UUID uuid = player.getUUID();
        if (servantsSet != null && servantsSet.containsKey(uuid)) {
            Pair<short[], Set<LivingEntity>> pair = servantsSet.get(uuid);
            //Set transient context
            removeFromMap = true;
            if (--pair.getA()[0] == 0) servantsSet.remove(uuid);
            if (onRemove != null && !pair.getB().isEmpty())
                for (LivingEntity entity : pair.getB())
                    onRemove.accept(entity);
            removeFromMap = false;
        }
    }

    public static Set<LivingEntity> getServantsSet(ServerPlayer player) {
        Pair<short[], Set<LivingEntity>> pair = servantsSet.get(player.getUUID());
        return pair == null ? null : pair.getB();
    }

    public static void changeAttribute(LivingEntity servants, Attribute attribute, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = servants.getAttribute(attribute);
        if (instance != null) {
            ResourceLocation name = ForgeRegistries.ENTITY_TYPES.getKey(servants.getType());
            //If it hasn't been registered yet and happens to appear here get fuck...
            if (name == null) return;
            UUID uuid = UUID.nameUUIDFromBytes((operation.name() + name).getBytes());

            if (removeFromMap) {
                VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
                if (modifier != null) {
                    if (modifier.getAmount() - value > 0) modifier.addValue(-value, instance);
                    else instance.removeModifier(uuid);
                }
            } else {
                AttributeModifier modifier = instance.getModifier(uuid);
                if (modifier != null) ((VariableAttributeModifier) modifier).addValue(value, instance);
                else instance.addTransientModifier(new VariableAttributeModifier(uuid, "ServantsReinforcement", value, operation));
            }
        }
    }
}
