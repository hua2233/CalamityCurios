package hua223.calamity.integration.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseCurio extends Item implements ICurioItem {
    protected final boolean timeRun;
    private final boolean hasEvent;
    private final boolean hasConflictChain;

    protected BaseCurio(Properties properties) {
        super(properties);
        timeRun = startClientTick() || startServerTick();
        hasEvent = !collectEvents().isEmpty();
        Class<?> c = getClass();
        hasConflictChain = c.isAnnotationPresent(ConflictChain.class);
        if (hasConflictChain)
            ConflictChain.Conflict.registerRootToLink(c.getAnnotation(ConflictChain.class));
    }

    public static void playerStorage(ServerPlayer player) {
        //The Curios author defaults to not processing the first frame, possibly to prevent excessive noise, so manual registration is required here
        //If FirstTick is set to true when a player instance is created, it should be manually reset, such low frequency events are acceptable
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ICuriosStorage storage)
                    storage.addToStorage(player);
            }
        });
    }

    public static void syncHealth(ServerPlayer player) {
        FoodData data = player.getFoodData();
        float health = Math.min(player.getHealth(), player.getMaxHealth());
        player.connection.send(new ClientboundSetHealthPacket(health,
            data.getFoodLevel(), data.getSaturationLevel()));
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        ICurioItem.super.onEquipFromUse(slotContext, stack);
        LivingEntity entity = slotContext.entity();
        //Build before Tick triggers to prevent NPE
        if (!entity.level.isClientSide && this instanceof ICuriosStorage storage)
            storage.addToStorage((ServerPlayer) slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.calamity$IsPlayer && !entity.calamity$Player.isLocalPlayer()) {
            if (hasConflictChain) {
                return ConflictChain.Conflict.noOccupied(
                    getClass().getAnnotation(ConflictChain.class), entity);
            }
            else
                return !CalamityHelp.hasCurio(slotContext.entity(), this);
        }
        return false;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.calamity$IsPlayer && !entity.calamity$Player.isLocalPlayer()) {
            ServerPlayer player = (ServerPlayer) entity;
            if (hasConflictChain) ConflictChain.Conflict.lockInConflict(
                getClass().getAnnotation(ConflictChain.class), entity);
            if (hasEvent) EventTypes.applyEvent(this, collectEvents(), player, true);
            equipHandle(player, stack);
        }
    }

    protected void equipHandle(ServerPlayer player, ItemStack stack) {
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.calamity$IsPlayer && !entity.calamity$Player.isLocalPlayer()) {
            ServerPlayer player = (ServerPlayer) entity;
            if (hasEvent) EventTypes.applyEvent(this, collectEvents(), player, false);
            unEquipHandle(player, stack);
            if (this instanceof ICuriosStorage storage) storage.removeStorage(player);
            if (hasConflictChain)
                ConflictChain.Conflict.unLockConflict(
                    getClass().getAnnotation(ConflictChain.class), player);
        }
    }

    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (entity != null) setAttributeModifiers(uuid, stack, modifiers, entity);
        return modifiers;
    }

    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {}

    protected boolean startServerTick() {
        return false;
    }

    protected void onPlayerTick(Player player) {
    }

    protected boolean startClientTick() {
        return false;
    }

    protected void onClientTick(Player player) {
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (timeRun) {
            LivingEntity entity = slotContext.entity();
            if (entity.calamity$IsPlayer) {
                boolean isClient = entity.level.isClientSide;
                if (startClientTick() && isClient) onClientTick(entity.calamity$Player);
                else if (startServerTick() && !isClient) onPlayerTick(entity.calamity$Player);
            }
        }
    }

    private List<Method> collectEvents() {
        Class<?> curio = this.getClass();
        List<Method> methods = new ArrayList<>(4);
        int modifier = 17;
        for (Method method : curio.getDeclaredMethods()) {
            if ((modifier & method.getModifiers()) == modifier
                && method.getReturnType() == void.class
                && method.isAnnotationPresent(ApplyEvent.class)) {
                methods.add(method);
            }
        }

        return methods;
    }

    public void onLogOut(Player player) {}

    @Retention(RetentionPolicy.RUNTIME) //hua223
    @Target(ElementType.METHOD)
    protected @interface ApplyEvent {
        int value() default 200;
    }
}
