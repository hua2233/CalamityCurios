package hua223.calamity.integration.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import hua223.calamity.events.EventTypes;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.CuriosConflictMap;
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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Arrays;
import java.util.UUID;

public abstract class BaseCurio extends Item implements ICurioItem {
    protected final boolean enableTick;
    private final boolean hasEvent;
    private final int hash;

    protected BaseCurio(Properties properties) {
        super(properties);
        //initialization attribute
        Class<?> c = getClass();
        hasEvent = !EventTypes.collectEvents(c).isEmpty();

        do {
            if (Arrays.stream(c.getDeclaredMethods()).anyMatch(method ->
                method.getParameterTypes().length == 1 &&
                method.getParameterTypes()[0] == Player.class &&
                (method.getName().equals("onPlayerTick") || method.getName().equals("onClientTick")))) break;
            else c = c.getSuperclass();
        } while (c != BaseCurio.class);

        enableTick = c != BaseCurio.class;
        if (this instanceof IKeyDataPackResponse response)
            response.registerResponseKeyMapping();
        hash = super.hashCode();
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
        if (!entity.level().isClientSide && this instanceof ICuriosStorage storage)
            storage.addToStorage((ServerPlayer) slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        return entity.calamity$IsPlayer && CuriosConflictMap.noOccupied(this, entity);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.calamity$IsPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            CuriosConflictMap.lock(this, player, true);
            if (hasEvent) EventTypes.applyEvent(this, player, true);
            equipHandle(player, stack);
        }
    }

    protected void equipHandle(ServerPlayer player, ItemStack stack) {
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.calamity$IsPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            if (hasEvent) EventTypes.applyEvent(this, player, false);
            unEquipHandle(player, stack);
            if (this instanceof ICuriosStorage storage) storage.removeStorage(player);//!newStack.is(this) &&
            CuriosConflictMap.lock(this, player, false);
        }
    }

    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (entity != null && (entity.level().isClientSide ||
            (!(this instanceof ICuriosStorage) || ((ICuriosStorage) this).getMemory(entity) != null)))
            setAttributeModifiers(uuid, stack, modifiers, entity);
        return modifiers;
    }

    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {}

    protected void onPlayerTick(Player player) {}

    protected void onClientTick(Player player) {}

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (enableTick && slotContext.entity().calamity$IsPlayer) {
            LivingEntity player = slotContext.entity();
            if (player.level().isClientSide) onClientTick(player.calamity$Player);
            else onPlayerTick(player.calamity$Player);
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
