package hua223.calamity.util;

import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerServantsManager {
    private static Map<UUID, Pair<short[], Set<LivingEntity>>> servantsSet;
    private static byte cleanupCycle = 0;
    private static boolean removeFromMap;

    public static void loadPlayerServantsEntity(ServerPlayer player, Consumer<LivingEntity> onJoin) {
        ServerLevel level = player.getLevel();
        Pair<short[], Set<LivingEntity>> set;
        if (servantsSet == null) {
            servantsSet = new Object2ObjectOpenHashMap<>();
            set = new Pair<>(new short[1], new ObjectOpenHashSet<>());
            servantsSet.put(player.getUUID(), set);
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
                if (++cleanupCycle == 4) {
                    for (UUID uuid : servantsSet.keySet()) {
                        ServerPlayer player1 = playerList.getPlayer(uuid);
                        if (player1 == null || player1.isDeadOrDying()) {
                            servantsSet.remove(uuid);
                            if (servantsSet.isEmpty()) {
                                servantsSet = null;
                                return true;
                            }
                        }
                    }

                    cleanupCycle = 0;
                }
                return false;
            }, 100);
        } else if (servantsSet.containsKey(player.getUUID())) {
            set = servantsSet.get(player.getUUID());
        } else {
            set = new Pair<>(new short[1], new ObjectOpenHashSet<>());
            servantsSet.put(player.getUUID(), set);
        }

        Set<LivingEntity> entities = set.getB();
        set.getA()[0]++;
        if (!entities.isEmpty()) for (LivingEntity e : entities) onJoin.accept(e);

        for (Entity entity : level.getEntities().getAll())
            if (!entity.isRemoved() && entity instanceof LivingEntity && entity instanceof OwnableEntity servants
                && !entities.contains(entity) && servants.getOwner() == player) {
                LivingEntity living = (LivingEntity) entity;
                entities.add(living);
                if (onJoin != null) onJoin.accept(living);
            }
    }

    public static void removePlayerServantsEntity(ServerPlayer player, Consumer<LivingEntity> onRemove) {
        UUID uuid = player.getUUID();
        if (servantsSet != null && servantsSet.containsKey(uuid)) {
            Pair<short[], Set<LivingEntity>> pair = servantsSet.get(uuid);
            //Set transient context
            removeFromMap = --pair.getA()[0] == 0;
            if (removeFromMap) servantsSet.remove(uuid);
            if (onRemove != null && !pair.getB().isEmpty())
                for (LivingEntity entity : pair.getB())
                    onRemove.accept(entity);
            removeFromMap = false;
        }
    }

    public static void changeAttribute(LivingEntity servants, Attribute attribute, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = servants.getAttribute(attribute);
        if (instance != null) {
            ResourceLocation name = ForgeRegistries.ENTITY_TYPES.getKey(servants.getType());
            //If it hasn't been registered yet and happens to appear here get fuck...
            if (name == null) return;
            UUID uuid = UUID.nameUUIDFromBytes((operation.name() + name).getBytes());
            if (removeFromMap) {
                instance.removeModifier(uuid);
            } else {
                AttributeModifier modifier = instance.getModifier(uuid);
                if (modifier != null) ((VariableAttributeModifier) modifier).addValue(value, instance);
                else instance.addTransientModifier(new VariableAttributeModifier(uuid, "ServantsReinforcement", value, operation));
            }
        }
    }
}
