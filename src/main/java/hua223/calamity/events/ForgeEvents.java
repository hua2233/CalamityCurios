package hua223.calamity.events;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.Decks;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.config.CalamityConfigHelper;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.render.Item.CrusherRender;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.render.entity.CrystallizationRenderLayer;
import hua223.calamity.render.entity.PurpleFlames;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static hua223.calamity.main.CalamityCurios.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    private static final ResourceLocation ENCHANTMENT = CalamityCurios.ModResource("calamity_enchantment");

    @SubscribeEvent
    public static void addCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer player) {
            addCapabilityIfAbsent(player, CalamityCapProvider.ADRENALINE, "adrenaline", event);
            addCapabilityIfAbsent(player, CalamityCapProvider.RAGE, "rage", event);
            addCapabilityIfAbsent(player, CalamityCapProvider.CALAMITY, "calamity", event);
        }
    }

    @SubscribeEvent
    public static void addCurseCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (!event.getCapabilities().containsKey(ENCHANTMENT)) {
            EnchantmentProvider.addIfCan(ENCHANTMENT, event);
        }
    }

    private static void addCapabilityIfAbsent(ServerPlayer player, CalamityCapProvider<?> provider, String name, AttachCapabilitiesEvent<Entity> event) {
        if (!player.getCapability(provider.getCapabilityType()).isPresent()) {
            event.addCapability(CalamityCurios.ModResource(name), provider.clone());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        for (SpellType type : SpellType.values()) type.init(player);

        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage -> rage.syncData(player));

        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(a -> a.syncData(player));

        CalamityCapProvider.CALAMITY.getCapabilityFrom(player).ifPresent(cap -> cap.syncData(player));

        EnchantmentProvider.syncRenderData(player);

        BaseCurio.playerStorage(player);

        for (MobEffectInstance instance : player.getActiveEffectsMap().values())
            if (instance.getEffect() instanceof IEffectsCallBack callBack)
                callBack.onLoad(instance, player);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        for (SpellType type : SpellType.values()) type.init(event.getPlayer());
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        DelayRunnable.setDist(event);
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
        if (event.isWasDeath()) {
            oldPlayer.reviveCaps();
            CalamityCapProvider.RAGE.getCapabilityFrom(oldPlayer).ifPresent(
                rage -> CalamityCapProvider.RAGE.getCapabilityFrom(player)
                    .ifPresent(newRage -> newRage.deathActivation(rage, player)));

            CalamityCapProvider.ADRENALINE.getCapabilityFrom(oldPlayer).ifPresent(
                a -> CalamityCapProvider.ADRENALINE.getCapabilityFrom(player)
                    .ifPresent(newA -> newA.deathActivation(a, player)));

            //Remove And NewCreate
            CuriosEventHandler.removePlayerEventHandler(oldPlayer);
            GlobalCuriosStorage.removePlayerStorage(oldPlayer);
            BaseCurio.playerStorage(player);
            CalamityConfigHelper.remove(player);
            VariableAttributeModifier.readOldValuesOfDeath(player, oldPlayer);
        }
    }

    @SubscribeEvent
    public static void canKnockBack(LivingKnockBackEvent event) {
        event.setCanceled(CalamityHelp.getCalamityFlag(event.getEntity(), 3));
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

//    @SubscribeEvent
//    public static void onVillagerTrades(VillagerTradesEvent event) {
//        if (event.getType() == VillagerProfession.FARMER) {
//            event.getTrades().get(4).add((trader, rand) ->
//                new MerchantOffer(
//                    new ItemStack(Items.RED_MUSHROOM_BLOCK, 1),
//                    new ItemStack(Items.NETHER_WART, 10),
//                    new ItemStack(CalamityItems.ODD_MUSHROOM.get(), 1),
//                    1, 10, 0.1f));
//        }
//    }

    @SubscribeEvent
    public static void onExpired(final MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() instanceof IEffectsCallBack properties)
            properties.onRemove(instance, event.getEntity());
    }

    //SpellEnchant
    @SubscribeEvent
    public static void onMainHandItemChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.MAINHAND) return;
        LivingEntity entity = event.getEntity();
        if (entity.calamity$IsPlayer) {
            Item item = CalamityItems.ATARAXIA.get();
            if (event.getTo().getItem() == item) CalamityHelp.setCalamityFlag(entity, 6, true);
            else if (event.getFrom().getItem() == item) CalamityHelp.setCalamityFlag(entity, 6, false);

            SpellType.EPHEMERAL.onTrigger(event, SpellType.TriggerType.MAIN_HAND_ITEM_CHANGE);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getCrafting().getItem() instanceof Decks decks) {
            Item rope = decks.getUnsealingRope();
            if (event.getInventory().hasAnyMatching(stack -> stack.is(rope)))
                decks.unblock(event.getCrafting());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvent {
        public static boolean canFluidWalk;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                if (canFluidWalk) CalamityHelp.applyFluidWalk(Minecraft.getInstance().player);
            } else {
                DelayRunnable.onTick(false);
                RenderUtil.updateGlobal();
                ClientInteraction.longPressToCheck();
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            ClientInteraction.onKeyDown();
        }

        @SubscribeEvent
        public static void onLongPress(InputEvent.InteractionKeyMappingTriggered event) {
            ClientInteraction.cancelResponse(event);
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
        @SuppressWarnings("removal")
        public static void onLevelRender(RenderLevelLastEvent event) {
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

        @SubscribeEvent
        public static void renderGui(RenderGuiEvent.Post event) {
            CrystallizationRenderLayer.renderTextureOverlay(event);
        }
    }
//    @SubscribeEvent
//    public static void onLootTableLoad(LootTableLoadEvent event) {
//        if (event.getName().equals(EntityType.ZOMBIE.getDefaultLootTable())) {
//            LootPool.Builder builder = LootPool.lootPool()
//                .name("diamond_loot")
//                .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
//                .setBonusRolls(0);
//
//            event.getTable().addPool(builder.build());
//        }
//    }
}
