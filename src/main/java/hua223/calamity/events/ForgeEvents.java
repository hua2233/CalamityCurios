package hua223.calamity.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.capability.RuneEnchantment;
import hua223.calamity.events.levelevent.client.ClientLevelEvent;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.mixed.ICalamityFontSetting;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.rune.IRuneReinforcement;
import hua223.calamity.render.CalamityOutlineRenderer;
import hua223.calamity.render.CalamityPsychedelicRenderer;
import hua223.calamity.render.IPlayerPostRenderer;
import hua223.calamity.render.font.CalamityFont;
import hua223.calamity.render.item.ExhumedDecoratorSystem;
import hua223.calamity.render.entity.PurpleFlames;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStreamReader;
import java.util.Optional;

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

        for (MobEffectInstance instance : player.getActiveEffectsMap().values())
            if (instance.getEffect() instanceof IEffectsCallBack callBack)
                callBack.onAdd(instance, player, null);
    }

    @SubscribeEvent
    @SuppressWarnings("ALL")
    public static void onServerStart(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        DelayRunnable.setDist(server);
        RecipeManager manager = server.getRecipeManager();
        RegistryAccess access = server.registryAccess();
        DamageSupplier.onServerStart(access);
        Optional<Resource> resource = server.getResourceManager().getResource(CalamityCurios.ModResource("modify_recipe_nbt.json"));
        //Load Recipe Nbt
        if (resource.isPresent()) {
            try (var inputStream = resource.get().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(inputStream));
                for (JsonElement element : json.getAsJsonArray()) {
                    JsonObject object = element.getAsJsonObject();
                    JsonElement ids = object.get("recipes_id");
                    ResourceLocation[] locations;
                    if (ids.isJsonArray()) {
                        JsonArray array = ids.getAsJsonArray();
                        locations = new ResourceLocation[array.size()];
                        for (int i = 0; i < locations.length; i++)
                            locations[i] = CalamityCurios.resource(array.get(i).getAsString());
                    } else locations = new ResourceLocation[]{CalamityCurios.resource(ids.getAsString())};

                    CompoundTag tag = null;
                    for (ResourceLocation id : locations) {
                        Optional<? extends Recipe<?>> optional = manager.byKey(id);
                        if (optional.isPresent()) {
                            Recipe<?> recipe = optional.get();
                            tag = TagParser.parseTag(object.get("nbt").getAsString());
                            recipe.getResultItem(access).getOrCreateTag().merge(tag);
                        }
                    }
                }
            } catch (Exception e) {
                CalamityCurios.LOGGER.error("Loaded recipe nbt failure");
            }
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
        ServerPlayer player = (ServerPlayer) event.getOriginal();
        player.reviveCaps();
        CuriosConflictMap.delete(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void lastPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().Calamity$Player.onClone(event.getOriginal(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onAddEffect(final MobEffectEvent.Added event) {
        MobEffectInstance instance = event.getEffectInstance();
        MobEffect effect = instance.getEffect();

        if (effect instanceof IEffectsCallBack properties)
            properties.onAdd(instance, event.getEntity(), event.getEffectSource());
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
            triggerEnchant(event.getFrom(), player, slot,false);
            triggerEnchant(event.getTo(), player, slot, true);
        }
    }

    private static void triggerEnchant(ItemStack stack, ServerPlayer player, EquipmentSlot slot, final boolean to) {
        Optional<RuneEnchantment> lazyOptional = stack.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).resolve();
        if (lazyOptional.isEmpty()) return;

        RuneEnchantment enchantment = lazyOptional.get();
        IRuneReinforcement rune = enchantment.getRunes();
        if (rune != null && rune.isEffectiveSlot(slot)) rune.onSlotChange(to, stack, player);
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
        public static void renderCalamityTooltip(RenderTooltipEvent.Pre event) {
            ItemStack stack = event.getItemStack();
            CompoundTag tag = stack.getTag();

            if (tag != null) {
                int type = tag.getInt(CalamityHelp.FONT_FLAG);
                if (type != 0) {
                    ((ICalamityFontSetting) (event)).calamity$SetFont(CalamityFont.fromTypeSetting(event.getComponents(), tag, type));
                }
            }
        }

        @SubscribeEvent
        public static void onClientClone(ClientPlayerNetworkEvent.Clone event) {
            event.getNewPlayer().Calamity$Player.onClone(event.getOldPlayer(), true);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void computeBossRushFog(ViewportEvent.ComputeFogColor event) {
            if (ClientLevelEvent.getRender() != null)
                ClientLevelEvent.getRender().setSkyFogColor(event);
        }

        @SubscribeEvent
        public static void screenShakeEffect(ViewportEvent.ComputeCameraAngles event) {
            ClientLevelEvent.screenShakeHandle(event);
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
            if (CalamityHelp.getClientCalamity().freeze) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }

        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, EntityModel<? extends LivingEntity>> event) {
            if (event.getEntity().calamity$IsPlayer && event.getEntity().calamity$Player.Calamity$Player.disableLegRendering) {
                Minecraft.getInstance().getEntityRenderDispatcher().setRenderShadow(false);
                HumanoidModel<Player> model = (HumanoidModel<Player>) event.getRenderer().getModel();
                model.leftLeg.visible = false;
                model.rightLeg.visible = false;
            }

            CalamityPsychedelicRenderer.psychedelic(event);
        }

        @SubscribeEvent
        public static void afterLivingRender(RenderLivingEvent.Post<? extends LivingEntity, EntityModel<? extends LivingEntity>> event) {
            PurpleFlames.renderFlame(event);
        }

        @SubscribeEvent
        public static void onLevelRender(RenderLevelStageEvent event) {
            CalamityOutlineRenderer.renderPerspective(event);
        }

        @SubscribeEvent
        public static void onRegisterItemDecorator(RecipesUpdatedEvent event) {
            ExhumedDecoratorSystem.registerExhumedItemDecorator(event.getRecipeManager());
        }

        @SubscribeEvent
        public static void afterPlayerRender(RenderPlayerEvent.Post event) {
            IPlayerPostRenderer renderer = event.getEntity().Calamity$Player.getRenderer();
            if (renderer != null) renderer.render(event);
        }
    }
}
