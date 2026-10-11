package hua223.calamity.util;

import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

//Refactoring
public class PlayerServantsManager {
    private final Set<LivingEntity> servants;

    private final ObjectArrayList<IServantsReinforcement> reinforcements;

    private static Map<UUID, PlayerServantsManager> managerMap;

    private PlayerServantsManager(ServerPlayer player) {
        //Load All Servants From Current World
        reinforcements = new ObjectArrayList<>();
        servants = new ObjectArraySet<>(getPlayerServants(player));

        if (managerMap == null) {
            managerMap = new Object2ObjectOpenHashMap<>(4);
            MinecraftForge.EVENT_BUS.register(PlayerServantsManager.class);
        }

        managerMap.put(player.getUUID(), this);
    }

    public static void addServantsReinforcement(ServerPlayer player, IServantsReinforcement reinforcement) {
        UUID uuid = player.getUUID();
        PlayerServantsManager manager = managerMap != null ? managerMap.get(uuid) : null;
        if (manager == null) manager = new PlayerServantsManager(player);
        manager.addReinforcement(player, reinforcement);
    }

    public static void removeServantsReinforcement(ServerPlayer player, IServantsReinforcement reinforcement) {
        if (managerMap != null) {
            PlayerServantsManager manager = managerMap.get(player.getUUID());
            if (manager != null) manager.removeReinforcement(player, reinforcement);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living && living instanceof
            OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {

            PlayerServantsManager manager = managerMap.get(player.getUUID());
           if (manager == null) return;

           manager.servants.add(living);
           for (IServantsReinforcement reinforcement : manager.reinforcements)
               reinforcement.modifyServant(true, player, living);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();

        //仆从以任何方式的卸载只要需求被保存则应该移除属性增幅
        if (entity instanceof LivingEntity living && living instanceof
            OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {

            PlayerServantsManager manager = managerMap.get(player.getUUID());
            if (manager == null) return;

            manager.servants.remove(living);
            Entity.RemovalReason removalReason = entity.getRemovalReason();
            //TransientModifier 不会被序列化保存，如果它本身只要瞬态修饰符则无需额外处理，让原本跳过处理即可
            if (removalReason.shouldSave()) for (IServantsReinforcement reinforcement : manager.reinforcements)
                if (!reinforcement.onlyTransientModifier()) reinforcement.modifyServant(false, player, living);

        } else if (entity instanceof ServerPlayer) {
            switch (entity.getRemovalReason()) {
                //死亡或者退出时清理，在维度穿越时不清理
                case KILLED, DISCARDED, UNLOADED_WITH_PLAYER -> {
                    PlayerServantsManager manager = managerMap.remove(entity.getUUID());
                    if (manager != null) {
                        ServerPlayer player = (ServerPlayer) entity;
                        for (LivingEntity servant : manager.servants) {
                            for (IServantsReinforcement reinforcement : manager.reinforcements)
                                reinforcement.modifyServant(false, player, servant);
                        }

                        tryClear();
                    }
                }
            }
        }
    }

    private static void tryClear() {
        if (managerMap.isEmpty()) {
            //如果在防抖期内填充，则不会触发事件移除
            DelayRunnable.addOrReset(300, PlayerServantsManager.class, () -> {
                if (managerMap.isEmpty()) {
                    managerMap = null;
                    MinecraftForge.EVENT_BUS.unregister(PlayerServantsManager.class);
                }
            });
        }
    }

    public final void addReinforcement(ServerPlayer player, IServantsReinforcement reinforcement) {
        reinforcements.add(reinforcement);
        for (LivingEntity servant : servants) reinforcement.modifyServant(true, player, servant);
    }

    public final void removeReinforcement(ServerPlayer player, IServantsReinforcement reinforcement) {
        Iterator<IServantsReinforcement> iterator = reinforcements.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == reinforcement) {
                iterator.remove();

                for (LivingEntity servant : servants) reinforcement.modifyServant(false, player, servant);

                if (reinforcements.isEmpty()) {
                    managerMap.remove(player.getUUID());
                    tryClear();
                }

                return;
            }
        }
    }

    public static List<? extends LivingEntity> getPlayerServants(ServerPlayer player) {
       return player.serverLevel().getEntities(EntityTypeTest.forClass(LivingEntity.class),
            possibleEntity -> possibleEntity.isAlive() && possibleEntity instanceof OwnableEntity entity && entity.getOwner() == player);
    }

    //onlyTransientModifier
    public static void changeAttribute(boolean apply, LivingEntity servants, Attribute attribute, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = servants.getAttribute(attribute);
        if (instance != null) {
            ResourceLocation name = ForgeRegistries.ENTITY_TYPES.getKey(servants.getType());
            //If it hasn't been registered yet and happens to appear here get fuck...
            if (name == null) return;
            UUID uuid = UUID.nameUUIDFromBytes((operation.name() + name).getBytes());

            if (apply) {
                AttributeModifier modifier = instance.getModifier(uuid);
                if (modifier != null) ((VariableAttributeModifier) modifier).addValue(value, instance);
                else instance.addTransientModifier(new VariableAttributeModifier(uuid, "ServantsReinforcement", value, operation));
            } else {
                VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
                if (modifier != null) {
                    if (modifier.getAmount() - value > 0.001f) modifier.addValue(-value, instance);
                    else instance.removeModifier(uuid);
                }
            }
        }
    }
}
