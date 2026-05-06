package hua223.calamity.events;

import hua223.calamity.capability.CalamityCap;
import hua223.calamity.capability.CurseEnchantment;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.EventTypes;
import hua223.calamity.integration.curios.MethodHandlerSorter;
import hua223.calamity.integration.curios.item.Calamity;
import hua223.calamity.integration.curios.item.DeitiesRampart;
import hua223.calamity.integration.curios.listeners.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.config.CalamityConfigHelper;
import hua223.calamity.register.effects.Bounding;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.register.entity.projectiles.MiniDragonBorn;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.render.CurseFont;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.common.util.LazyOptional;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.Items.CalamityItems.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEventHandler {
    private static final Map<UUID, Map<EventTypes<?>, List<MethodHandlerSorter>>> PLAYER_ACTIVE_EVENT
        = new Object2ObjectOpenHashMap<>(2);

    private CuriosEventHandler() {}

    static void removePlayerEventHandler(ServerPlayer player) {
        PLAYER_ACTIVE_EVENT.computeIfPresent(player.getUUID(), (k, library) -> {
            for (Map.Entry<EventTypes<?>, List<MethodHandlerSorter>> entry : library.entrySet())
                entry.getKey().removeBatch(entry.getValue());
            return null;
        });
    }

    public static void addEvent(EventTypes<?> eventType, MethodHandlerSorter sorter, ServerPlayer player) {
        PLAYER_ACTIVE_EVENT.compute(player.getUUID(), (uuid, eventMap) -> {
            if (eventMap == null) eventMap = new Object2ObjectOpenHashMap<>(EventTypes.getEventTypeTotal());
            eventMap.compute(eventType, (key, events) -> {
                if (events == null) events = new ArrayList<>(16);
                events.add(sorter);
                events.sort(MethodHandlerSorter::compareTo);
                return events;
            });
            return eventMap;
        });
    }

    public static void removeEvent(EventTypes<?> type, MethodHandlerSorter sorter, ServerPlayer player) {
        PLAYER_ACTIVE_EVENT.computeIfPresent(player.getUUID(), (uuid, eventMap) -> {
            eventMap.computeIfPresent(type, (t, events) -> {
                events.remove(sorter);
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
                        if (event.isCanceled()) break;
                        handler.invoke().invokeExact(event);
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
        if (!entity.level().isClientSide && entity.calamity$IsPlayer)
            onPlayerHurt(event, (ServerPlayer) event.getEntity());
        else if (event.getSource().getEntity() instanceof ServerPlayer player)
            onPlayerAttack(event, player);
    }

    public static void onPlayerHurt(final LivingHurtEvent event, ServerPlayer player) {
        boolean isFall = event.getSource().is(DamageTypeTags.IS_FALL);
        if (isFall && player.hasEffect(CalamityEffects.CALCIUM.get())) {
            event.setCanceled(true);
        } else {
            HurtListener listener = dispatch(player, EventTypes.HURT, event, player);
            if (listener != null) {
                if (!listener.isCanceled()) return;
                Calamity.sunkCurse(listener);
                DeitiesRampart.rampartGuard(listener);
                if (!listener.isCanceled()) return;

                if (SpellType.TriggerType.canTriggerEnchant(player, SpellType.TriggerType.PLAYER_HURT)) {
                    SpellType.TriggerType.listenerTriggerEnchant(listener);
                    if (!listener.isCanceled()) return;
                }

                if (isFall && listener.player.hasEffect(CalamityEffects.BOUNDING.get())) listener.amplifier -= 0.4f;
                listener.amplifier -= (float) player.getAttributeValue(
                    CalamityAttributes.INJURY_OFFSET.get()) - (1 + listener.player.calamity$EffectFragile);
                event.setAmount(listener.getCorrectionValue());
            } else if (SpellType.TriggerType.canTriggerEnchant(player, SpellType.TriggerType.PLAYER_HURT)) {
                listener = SpellType.TriggerType.listenerTriggerEnchant(EventTypes.HURT, event, player);
                if (!listener.isCanceled()) event.setAmount(listener.getCorrectionValue());
            }
        }
    }

    public static void onPlayerAttack(final LivingHurtEvent event, ServerPlayer player) {
        //Calculate universal amplification
        double common = player.getAttributeValue(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get()) - 1;
        double amplifier;
        Boolean isCalamityCriticalHits = Boolean.FALSE;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            double criticalHits = (player.getAttributeValue(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get()) - 1) + common;
            amplifier = player.getAttributeValue(CalamityAttributes.FAR_ATTACK.get());

            if (ProjectileHitListener.isSpiritOriginCritical()) {
                amplifier += (2.5 + criticalHits);
                isCalamityCriticalHits = Boolean.TRUE;
            } else if (criticalHits > 0 && criticalHits >= player.getRandom().nextDouble()) {
                amplifier += 2;
                isCalamityCriticalHits = Boolean.TRUE;
            }
        } else {
            amplifier = player.getAttributeValue(CalamityAttributes.CLOSE_RANGE.get());

            double value = (player.getAttributeValue(CalamityAttributes.CLOSE_CRITICAL_STRIKE_CHANCE.get()) - 1) + common;
            if (value > 0 && value >= player.getRandom().nextDouble()) {
                isCalamityCriticalHits = Boolean.TRUE;
                amplifier += 2f;
            }
        }

        amplifier += (float) player.getAttributeValue(CalamityAttributes.DAMAGE_UP.get()) - 1;
        amplifier += event.getEntity().calamity$EffectFragile;

        PlayerAttackListener listener = dispatch(player, EventTypes.ATTACK, player, event, isCalamityCriticalHits);
        //No Listener
        if (listener == null) {
            if (SpellType.TriggerType.canTriggerEnchant(player, SpellType.TriggerType.PLAYER_ATTACK)) {
               listener = SpellType.TriggerType.listenerTriggerEnchant(EventTypes.ATTACK, player, event, isCalamityCriticalHits);
               if (!listener.isCanceled()) event.setAmount(listener.getCorrectionValue());

            } else event.setAmount((event.getAmount() * (float) amplifier));
        } else if (!listener.isCanceled()) {
            if (SpellType.TriggerType.canTriggerEnchant(player, SpellType.TriggerType.PLAYER_ATTACK)) {
                SpellType.TriggerType.listenerTriggerEnchant(listener);
                if (listener.isCanceled()) return;
            }

            listener.amplifier += (float) amplifier;
            event.setAmount(listener.getCorrectionValue());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        Entity entity = event.getTarget();
        if (entity.level().isClientSide || !(entity instanceof LivingEntity)) return;

        CriticalHitCheckListener listener = dispatch(event.getEntity(), EventTypes.CRITICAL_HIT_CHECK, event);
        boolean hasListener = listener != null;
        if ((hasListener && listener.isCriticalHit()) || (!hasListener && event.isVanillaCritical()))
            dispatch(event.getEntity(), EventTypes.CRITICAL_HIT_TRIGGER, event);
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
                if (listener == null || listener.isCanceled()) return;

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
            if (!cooldowns.isOnCooldown(stemCells) && CalamityHelp.hasCurio(player, stemCells)) {
                Level level = player.level();
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
        if (entity.calamity$IsPlayer && !entity.level().isClientSide) {
            double value = entity.getAttributeValue(CalamityAttributes.AMMUNITION_ADD.get()) - 1;
            if (value != 0 && value >= entity.level().random.nextDouble())
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
        if (entity.calamity$IsPlayer && !entity.level().isClientSide) {
            ServerPlayer player = (ServerPlayer) entity;
            dispatch(player, EventTypes.DEATH, event, player, Boolean.TRUE);
            if (!event.isCanceled()) ConflictChain.Conflict.delete(player);
        } else if (event.getSource().getEntity() instanceof ServerPlayer player) {
            dispatch(player, EventTypes.DEATH, event, player, Boolean.FALSE);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.calamity$InactivationCount > 0) event.setCanceled(true);
        else if (entity.calamity$IsPlayer) {
            PlayerHealListener listener = dispatch(entity.calamity$Player, EventTypes.HEAL, event, entity.calamity$Player);
            if (listener != null && !listener.isCanceled()) event.setAmount(listener.getCorrectionValue());
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || CalamityCap.notHasCalamity()) return;
        LivingEntity target = event.getNewTarget();

        if (target != null && target.calamity$IsPlayer && (!(entity instanceof Enemy) || entity.getLastHurtByMob() != target) &&
            CalamityCap.isCalamity(target) && CalamityCap.isInverted(CalamityCap.CurseType.SILVA, target))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().is(Items.MUSHROOM_STEW) && FUNGAL_SYMBIOTE.isEquip(event.getEntity()))
            event.getEntity().addEffect(new MobEffectInstance(CalamityEffects.MUSHY.get(), 200));
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
        //Why does logging in also trigger????
        if (player != null) {
            logOutCuriosCallBack(player);
            RenderUtil.clearOldDecorator(player.level().getRecipeManager(), true);
            CurseFont.reSet();
            RenderUtil.Shaders.renderHighlightBlocks(true);
            Bounding.jumpPower = 1f;
            CalamityCap.reSet();
            ClientInteraction.clear();
        }
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
        CuriosApi.getCuriosInventory(player).ifPresent(modifiable -> {
            for (int i = 0; i < modifiable.getSlots(); i++) {
                IItemHandlerModifiable handler = modifiable.getEquippedCurios();
                for (int j = 0; j < handler.getSlots(); j++) {
                    ItemStack curio = handler.getStackInSlot(j);
                    if (!curio.isEmpty() && curio.getItem() instanceof BaseCurio base)
                        base.onLogOut(player);
                }
            }
        });
    }
}
