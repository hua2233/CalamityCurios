package hua223.calamity.events;

import hua223.calamity.capability.CalamityCap;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.EventTypes;
import hua223.calamity.integration.curios.item.Calamity;
import hua223.calamity.integration.curios.item.DeitiesRampart;
import hua223.calamity.integration.curios.listeners.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.config.CalamityConfigHelper;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.register.entity.projectiles.MiniDragonBorn;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.Items.CalamityItems.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEventHandler {
    private static final Map<UUID, Map<EventTypes<?>, List<MethodHandlerSorter>>> PLAYER_ACTIVE_EVENT
        = new Object2ObjectOpenHashMap<>(2);

    private CuriosEventHandler() {
    }

    static void removePlayerEventHandler(ServerPlayer player) {
        Map<EventTypes<?>, List<MethodHandlerSorter>> map = PLAYER_ACTIVE_EVENT.remove(player.getUUID());
        if (map != null)
            for (Map.Entry<EventTypes<?>, List<MethodHandlerSorter>> entry : map.entrySet())
                entry.getKey().removeBatch(entry.getValue());
    }

    public static void addEvent(EventTypes<?> eventType, MethodHandle handler, int priority, Item item, ServerPlayer player) {
        PLAYER_ACTIVE_EVENT.compute(player.getUUID(), (uuid, eventMap) -> {
            if (eventMap == null) eventMap = new Object2ObjectOpenHashMap<>(EventTypes.getEventTypeTotal());
            eventMap.compute(eventType, (key, events) -> {
                if (events == null) events = new ArrayList<>(16);
                events.add(new MethodHandlerSorter(handler, priority, item));
                events.sort(MethodHandlerSorter::compareTo);
                return events;
            });
            return eventMap;
        });
    }

    public static void removeEvent(EventTypes<?> type, MethodHandle handler, ServerPlayer player) {
        PLAYER_ACTIVE_EVENT.computeIfPresent(player.getUUID(), (uuid, eventMap) -> {
            eventMap.computeIfPresent(type, (t, events) -> {
                events.removeIf(sorter -> sorter.match(handler));
                return events.isEmpty() ? null : events;
            });
            return eventMap.isEmpty() ? null : eventMap;
        });
    }

    @Nullable
    private static <T extends BaseListener<?>> T dispatch(Player player, EventTypes<T> type, Object... argsContext) {
        Map<EventTypes<?>, List<MethodHandlerSorter>> library = PLAYER_ACTIVE_EVENT.get(player.getUUID());
        if (library != null) {
            List<MethodHandlerSorter> events = library.get(type);
            if (events != null) {
                //延迟按需构造，防止提前构造无用对象
                try {
                    T event = type.builderEvent(argsContext);
                    for (MethodHandlerSorter handler : events) {
                        if (event.isCanceled()) return null;
                        handler.handle().invokeExact(event);
                    }

                    return event;
                } catch (Throwable e) {
                    CalamityCurios.LOGGER.error("Fatal error occurred while handling the event");
                    throw new RuntimeException(e);
                }
            }
        }

        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerHurtOrAttack(final LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level.isClientSide && entity.calamity$IsPlayer)
            onPlayerHurt(event, (ServerPlayer) event.getEntity());
        else if (event.getSource().getEntity() instanceof ServerPlayer player)
            onPlayerAttack(event, player);
    }

    public static void onPlayerHurt(final LivingHurtEvent event, ServerPlayer player) {
        if (event.getSource().isFall() && player.hasEffect(CalamityEffects.CALCIUM.get())) {
            event.setCanceled(true);
        } else {
            HurtListener listener = dispatch(player, EventTypes.HURT, event, player);
            if (listener != null) {
                Calamity.sunkCurse(listener);
                DeitiesRampart.rampartGuard(listener);
                tryTriggerEnchant(player, event, SpellType.TriggerType.PLAYER_HURT);

                if (listener.player.hasEffect(CalamityEffects.BOUNDING.get())) listener.amplifier -= 0.4f;
                listener.amplifier -= (float) player.getAttributeValue(CalamityAttributes.INJURY_OFFSET.get()) - 1;
                event.setAmount(listener.getCorrectionValue());
            }
        }
    }

    public static void onPlayerAttack(final LivingHurtEvent event, ServerPlayer player) {
        PlayerAttackListener listener = dispatch(player, EventTypes.ATTACK, player, event);
        if (listener == null) return;

        tryTriggerEnchant(player, event, SpellType.TriggerType.PLAYER_ATTACK);
        double common = player.getAttributeValue(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get()) - 1;
        criticalHitsAttribute(listener, common);
        listener.amplifier += (float) listener.player.getAttributeValue(CalamityAttributes.DAMAGE_UP.get()) - 1;
        MobEffectInstance instance = listener.entity.getEffect(CalamityEffects.NATURE_PAIN.get());

        if (instance != null)
            listener.amplifier += (instance.getAmplifier() + 1) * 0.1f;
        event.setAmount(listener.getCorrectionValue());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        Entity entity = event.getTarget();
        if (!entity.level.isClientSide && entity instanceof LivingEntity) {
            CriticalHitListener listener = dispatch(event.getEntity(), EventTypes.CRITICAL_HIT, event);
            if (listener != null) listener.applyCallBack();
        }
    }

    @SubscribeEvent
    public static void onGetEffect(final MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player)
            dispatch(player, EventTypes.EFFECT, event, player);
    }

    @SubscribeEvent
    public static void onEntityJoin(final EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide) {
            //loadedFromDisk
            //Reapplying callbacks during disk loading does not trigger Effect related events for entities loaded from the disk
            if (event.loadedFromDisk() && event.getEntity() instanceof LivingEntity entity) {
                for (MobEffectInstance instance : entity.getActiveEffects())
                    if (instance.getEffect() instanceof IEffectsCallBack callBack)
                        callBack.onLoad(instance, entity);
            } else if (event.getEntity() instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
                ProjectileSpawnListener listener = dispatch(player, EventTypes.PROJECTILE_SPAWN, event, player, projectile);
                if (listener == null) return;

                if (listener.speedVectorAmplifier != 1)
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(listener.speedVectorAmplifier));

                if (listener.isArrow && listener.hurtAmplifier != 1)
                    listener.arrow.setBaseDamage(listener.arrow.getBaseDamage() * listener.hurtAmplifier);
            }
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(final ArrowLooseEvent event) {
        Player player = event.getEntity();
        if (!player.isLocalPlayer()) {
            Item stemCells = STEM_CELLS.get();
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(stemCells) && CalamityHelp.hasCurio(player, stemCells)) {
                Level level = player.level;
                if (level.random.nextDouble() < 0.3) {
                    level.addFreshEntity(MiniDragonBorn.of(level, player));
                    cooldowns.addCooldown(stemCells, 500);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.calamity$IsPlayer && !entity.level.isClientSide) {
            double value = entity.getAttributeValue(CalamityAttributes.AMMUNITION_ADD.get()) - 1;
            if (value != 0 && value >= entity.level.random.nextDouble())
                event.setProjectileItemStack(event.getProjectileItemStack().copy());
        }
    }

    @SubscribeEvent
    public static void onProjectileHit(final ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult result &&
            result.getEntity() instanceof LivingEntity target &&
            event.getProjectile().getOwner() instanceof ServerPlayer player)
            dispatch(player, EventTypes.PROJECTILE_HIT, event, player, target);
    }

    @SubscribeEvent
    public static void onDimensionChange(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().isLocalPlayer()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            dispatch(player, EventTypes.DIMENSION_CHANGE, event, player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDeath(final LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DeathListener listener = null;
        if (entity.calamity$IsPlayer && !entity.level.isClientSide) {
            ServerPlayer player = (ServerPlayer) entity;
            listener = dispatch(player, EventTypes.DEATH, event, player, Boolean.TRUE);
        } else if (event.getSource().getEntity() instanceof ServerPlayer player)
            listener = dispatch(player, EventTypes.DEATH, event, player, Boolean.FALSE);

        if (listener != null && listener.isPlayerDeath)
            ConflictChain.Conflict.delete(listener.player);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level.isClientSide) return;

        if (CalamityHelp.getCalamityFlag(entity, 5)) {
            event.setCanceled(true);
            return;
        }

        if (entity.calamity$IsPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            if (player.calamity$InactivationCount > 0) {
                event.setCanceled(true);
            } else {
                PlayerHealListener listener = dispatch(player, EventTypes.HEAL, event, player);
                if (listener != null) event.setAmount(event.getAmount() + listener.bonus * listener.amplification);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level.isClientSide || CalamityCap.notHasCalamity()) return;
        LivingEntity target = event.getNewTarget();

        if (target != null && target.calamity$IsPlayer && (!(entity instanceof Enemy) || entity.getLastHurtByMob() != target) &&
            CalamityCap.isCalamity(target) && CalamityCap.isInverted(CalamityCap.CurseType.SILVA, target))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().is(Items.MUSHROOM_STEW)) {
            LivingEntity entity = event.getEntity();
            if (FUNGAL_SYMBIOTE.isEquip(entity)) {
                entity.addEffect(new MobEffectInstance(CalamityEffects.MUSHY.get(), 200));
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        logOutCuriosCallBack(player);
        ConflictChain.Conflict.delete(player);
        removePlayerEventHandler(player);
        CalamityConfigHelper.remove(player);
        GlobalCuriosStorage.removePlayerStorage(player);
        for (SpellType type : SpellType.values())
            type.delete(player);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LocalPlayer player = event.getPlayer();
        if (player != null) {
            logOutCuriosCallBack(player);
            RenderUtil.clearOldDecorator(player.level.getRecipeManager(), true);
        }

        RenderUtil.Shaders.renderHighlightBlocks(true);
        CalamityCap.reSet();
        ClientInteraction.clear();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public static void onMoveInput(MovementInputUpdateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.isMovingSlowly() && CalamityHelp.getCalamityFlag(player, 10)) {
            againCalculateImpulse(event.getInput(), Mth.clamp(.3f + EnchantmentHelper.getSneakingSpeedBonus(player),
                0f, 1f) + (CalamityHelp.getCalamityFlag(player, 10) ? .5f : .3f));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void againCalculateImpulse(Input input, float factor) {
        if (input.up != input.down) input.forwardImpulse = input.up ? factor : -factor;
        if (input.left != input.right) input.leftImpulse = input.left ? factor : -factor;
    }

    @SubscribeEvent
    public static void setInvisible(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.calamity$IsPlayer && entity.calamity$Player.calamity$Invisible > 0)
            event.modifyVisibility(entity.calamity$Player.calamity$Invisible);
    }

    private static void logOutCuriosCallBack(Player player) {
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(modifiable -> {
            for (int i = 0; i < modifiable.getSlots(); i++) {
                ItemStack curio = modifiable.getStackInSlot(i);
                if (!curio.isEmpty() && curio.getItem() instanceof BaseCurio base)
                    base.onLogOut(player);
            }
        });

    }

    private static void criticalHitsAttribute(HurtListener listener, double common) {
        ServerPlayer player = listener.player;
        boolean explosion = false;
        if (listener.isFarAttack()) {
            double criticalHits = (player.getAttributeValue(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get()) - 1) + common;
            double amplifier = player.getAttributeValue(CalamityAttributes.FAR_ATTACK.get());

            if (ProjectileHitListener.isSpiritOriginCritical()) {
                listener.amplifier += (float) ((2.5 + criticalHits) + amplifier);
                explosion = true;
            } else if (criticalHits > 0 && criticalHits >= player.getRandom().nextDouble()) {
                listener.amplifier += (float) (amplifier + 2);
                explosion = true;
            }
        } else {
            listener.amplifier += (float) listener.player.getAttributeValue(CalamityAttributes.CLOSE_RANGE.get()) - 1;

            double value = (listener.player.getAttributeValue(CalamityAttributes.CLOSE_CRITICAL_STRIKE_CHANCE.get()) - 1) + common;
            if (value > 0 && value >= player.getRandom().nextDouble()) {
                explosion = true;
                listener.amplifier += 2f;
            }
        }

        if (explosion) applyExplosion(listener.player, listener.entity, player.level);
    }

    private static void tryTriggerEnchant(Player player, Event event, SpellType.TriggerType type) {
        player.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(
            enchantment -> {
                if (enchantment.isEffective()) enchantment.getRunes().onTrigger(event, type);
            });
    }

    private static void applyExplosion(ServerPlayer player, LivingEntity target, Level level) {
        ItemCooldowns cooldowns = player.getCooldowns();
        if (!cooldowns.isOnCooldown(EXTINCTION_VOID.get()) && CalamityHelp.hasCurio(player, EXTINCTION_VOID.get())) {
            level.explode(player, target.getX(), target.getY(), target.getZ(), 2f, Explosion.BlockInteraction.NONE);
            target.addEffect(new MobEffectInstance(CalamityEffects.BRIMSTONE_FLAMES.get(), 60));
            cooldowns.addCooldown(EXTINCTION_VOID.get(), 160);
        } else if (!cooldowns.isOnCooldown(ABADDON.get()) && CalamityHelp.hasCurio(player, ABADDON.get())) {
            if (cooldowns.isOnCooldown(ABADDON.get())) return;
            level.explode(player, target.getX(), target.getY(), target.getZ(),
                2f, Explosion.BlockInteraction.NONE);
            target.addEffect(new MobEffectInstance(CalamityEffects.BRIMSTONE_FLAMES.get(), 40));
            cooldowns.addCooldown(ABADDON.get(), 200);
        }
    }

    public record MethodHandlerSorter(MethodHandle handle, int priority, Item item) implements Comparable<MethodHandlerSorter> {
        @Override
        public int compareTo(@NotNull MethodHandlerSorter sorter) {
            return Integer.compare(priority, sorter.priority);
        }

        boolean match(MethodHandle handle) {
            return this.handle == handle;
        }
    }
}
