package hua223.calamity.events;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.integration.curios.Decks;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.render.Item.CrusherRender;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.render.entity.PurpleFlames;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static hua223.calamity.main.CalamityCurios.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    private static final ResourceLocation ENCHANTMENT = CalamityCurios.ModResource("calamity_enchantment");

    @SubscribeEvent
    public static void addCurseCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (!event.getCapabilities().containsKey(ENCHANTMENT))
            EnchantmentProvider.addIfCan(ENCHANTMENT, event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.Calamity$Player.logInServerInitialization();
        EnchantmentProvider.syncRenderData(player);

        for (MobEffectInstance instance : player.getActiveEffectsMap().values())
            if (instance.getEffect() instanceof IEffectsCallBack callBack)
                callBack.onLoad(instance, player);
    }

    @SubscribeEvent
    @SuppressWarnings("ALL")
    public static void onServerStart(ServerStartedEvent event) {
        DelayRunnable.setDist(event);
        //Load Deck Recipe Nbt
        RecipeManager manager = event.getServer().getRecipeManager();
        for (Optional<? extends Recipe<?>> optional : List.of(manager.byKey(
                CalamityCurios.ModResource("oracle_deck_unsealing")),
            manager.byKey(CalamityCurios.ModResource("tainted_deck_unsealing"))))
            if (optional.isPresent() && optional.get() instanceof ShapelessRecipe recipe) {
                ItemStack stack = recipe.getResultItem(null);
                if (stack.getItem() instanceof Decks decks && recipe.getIngredients().stream().flatMap(ingredient ->
                    Arrays.stream(ingredient.getItems())).map(ItemStack::getItem).anyMatch(item -> item == decks.getUnsealingRope()))
                    decks.unblock(stack);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onManaChange(ChangeManaEvent event) {
        float old = event.getOldMana();
        float new_ = event.getNewMana();
        if (old > new_) {
            float v = (float) event.getEntity().getAttributeValue(CalamityAttributes.MAGIC_REDUCTION.get());
            if (v > 1f) event.setNewMana(Mth.lerp(v - 1f, new_, old));
            else event.setNewMana(Math.max(0, old - (old - new_) * (2f - v)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) DelayRunnable.onTick(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void prePlayerClone(PlayerEvent.Clone event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        BossRushEvent.onPlayerReSpawn(player);
        ConflictChain.Conflict.delete(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void lastPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().Calamity$Player.onClone(event.getOriginal(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        BossRushEvent.onTeleport(event);
    }

    @SubscribeEvent
    public static void onAddEffect(final MobEffectEvent.Added event) {
        MobEffectInstance instance = event.getEffectInstance();
        MobEffect effect = instance.getEffect();

        if (effect instanceof IEffectsCallBack properties)
            properties.onAdd(instance, event.getEntity(), event.getEffectSource());
    }

    @SubscribeEvent
    public static void onRemove(final MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null) {
            MobEffect effect = instance.getEffect();
            if (effect instanceof IEffectsCallBack properties) {
                properties.onRemove(instance, event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onExpired(final MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() instanceof IEffectsCallBack properties)
            properties.onRemove(instance, event.getEntity());
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().calamity$IsPlayer) {
            EquipmentSlot slot = event.getSlot();
            ServerPlayer player = (ServerPlayer) event.getEntity().calamity$Player;
            //不提供ItemStack使用，如果修改了新的输入，会可能导致循环触发此事件
            if (event.getTo().getItem() instanceof IEquipmentInspection inspection
                && inspection.isEffectiveSlot(slot)) inspection.onEquip(player);
            else if (event.getFrom().getItem() instanceof IEquipmentInspection inspection
                && inspection.isEffectiveSlot(slot)) inspection.onUnEquip(player);


            //SpellEnchant
            triggerEnchant(event.getFrom(), player, false);
            triggerEnchant(event.getTo(), player, true);
        }
    }

    private static void triggerEnchant(ItemStack stack, ServerPlayer player, boolean to) {
        stack.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment -> {
            if (enchantment.isEffective()) enchantment.getRunes().onMainHandChange(to, stack, player);
        });
    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvent {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                DelayRunnable.onTick(false);
                RenderUtil.updateGlobal();
            }
        }

        @SubscribeEvent
        public static void onClientClone(ClientPlayerNetworkEvent.Clone event) {
            event.getNewPlayer().Calamity$Player.onClone(event.getOldPlayer(), true);
        }

        @SubscribeEvent
        public static void computeBossRushFog(ViewportEvent.ComputeFogColor event) {
            if (ClientRushEvent.isBossRushEventActivating())
                ClientRushEvent.BossRushSky.setSkyFogColor(event);
        }

        @SubscribeEvent
        public static void screenShakeEffect(ViewportEvent.ComputeCameraAngles event) {
            ClientRushEvent.screenShakeHandle(event);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void setFogEffect(ViewportEvent.RenderFog event) {
            if (event.getType() == FogType.WATER) {
                int scale = CalamityHelp.getClientCalamity().azureAbyssFlag;
                if (scale > 1) {
                    event.scaleNearPlaneDistance(scale);
                    event.scaleFarPlaneDistance(scale);
                    //Only cancellation will apply...
                    event.cancel();
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            ClientInteraction.checkDown(event.getKey());
        }

        @SubscribeEvent
        public static void onLongPress(InputEvent.InteractionKeyMappingTriggered event) {
            ClientInteraction.longPressResponse(event);
        }

        @SubscribeEvent
        public static void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
            RenderUtil.Shaders.psychedelic(event);
        }

        @SubscribeEvent
        public static void afterLivingRender(RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
            PurpleFlames.renderFlame(event);
        }

        @SubscribeEvent
        public static void onLevelRender(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
                RenderUtil.Shaders.renderBlockPerspective(event);
        }

        @SubscribeEvent
        public static void onRegisterItemDecorator(RecipesUpdatedEvent event) {
            RenderUtil.registerExhumedItemDecorator(event.getRecipeManager());
        }

        @SubscribeEvent
        public static void afterPlayerRender(RenderPlayerEvent.Post event) {
            if (CrusherRender.isRendering) CrusherRender.render(event);
            else if (YharimsCrystalRenderer.crystalRayRender)
                YharimsCrystalRenderer.renderYharimsCrystal(event);
        }
    }
}
