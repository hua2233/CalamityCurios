package hua223.calamity.events;

import hua223.calamity.integration.curios.item.NihilityQuiver;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.entity.*;
import hua223.calamity.register.entity.projectiles.*;
import hua223.calamity.register.gui.CalamityCurseScreen;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.particle.*;
import hua223.calamity.render.Item.TransformBakeModel;
import hua223.calamity.render.Item.YharimsCrystalRenderer;
import hua223.calamity.render.entity.FrozenRender;
import hua223.calamity.render.entity.PurpleFlames;
import hua223.calamity.render.hud.*;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.Set;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.Items.CalamityItems.NIHILITY_QUIVER;
import static hua223.calamity.register.gui.RegisterMenuType.CALAMITY_CURES;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void addPlayerAttribute(EntityAttributeModificationEvent event) {
        for (Attribute attribute : CalamityAttributes.getAll()) {
            if (!event.has(EntityType.PLAYER, attribute)) {
                event.add(EntityType.PLAYER, attribute);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegisterList.registerTier();
            RegisterList.clearRegisters();
            interMessage();
        });
    }

    @SubscribeEvent
    public static void onConfigSetUp(ModConfigEvent.Loading event) {
        CalamityConfig.onLoadConfigInfo(event.getConfig().getType());
    }

    @SubscribeEvent
    public static void onConfigReLoad(ModConfigEvent.Reloading event) {
        CalamityConfig.onLoadConfigInfo(event.getConfig().getType());
    }

    private static void interMessage() {
        final int size = 1;
        final String ID = "curios";
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.NECKLACE.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HANDS.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CURIO.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BACK.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BELT.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BRACELET.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BODY.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.RING.getMessageBuilder().size(size).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () ->
            new SlotTypeMessage.Builder("cards").size(3).priority(105).icon(
                CalamityCurios.ResourceOf(ID, "slot/card")).build());
        InterModComms.sendTo(ID, SlotTypeMessage.REGISTER_TYPE, () ->
            new SlotTypeMessage.Builder("calamity").size(1).priority(70).icon(
                CalamityCurios.ResourceOf(ID, "slot/calamity")).build());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientMod {
        @SubscribeEvent
        public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
            CuriosRendererRegistry.register(NIHILITY_QUIVER.get(), NihilityQuiver.Render::new);
            MenuScreens.register(CALAMITY_CURES.get(), CalamityCurseScreen::new);
        }

        @SubscribeEvent
        public static void onParticleRegister(RegisterParticleProvidersEvent event) {
            event.register(ParticleRegister.GOLDEN_LEMNISCATE.get(), new LemniscateParticle.GoldenFactory());
            event.register(ParticleRegister.ETERNITY_DUST.get(), EternityDust.DustProvider::new);
            event.register(ParticleRegister.SAKURA.get(), Sakura.Provider::new);
            event.register(ParticleRegister.PULSE.get(), Pulse.PulseProvider::new);
            event.register(ParticleRegister.SPARK.get(), SparkParticle.SparkProvider::new);
            event.register(ParticleRegister.GLOW_SPARK.get(),  GlowSparkParticle.GlowSparkProvider::new);
            event.register(ParticleRegister.POINT.get(), PointParticle.PointProvider::new);
        }

        @SubscribeEvent
        public static void registerProperties(RegisterClientReloadListenersEvent event) {
            ItemProperties.register(CalamityItems.NEBULOUS_CATACLYSM.get(), CalamityCurios.ModResource("cataclysm_hold"),
                (itemStack, clientLevel, livingEntity, i) ->
                    ClientInteraction.isLongPressActive() ? 1f : 0f);

            ItemProperties.register(CalamityItems.YHARIMS_CRYSTAL.get(), CalamityCurios.ModResource("yharims_active"),
                (itemStack, clientLevel, livingEntity, i) ->
                    YharimsCrystalRenderer.crystalRayRender ? 1f : 0f);
        }

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) {
            RenderUtil.Shaders.registerShaders(event);
        }

        @SubscribeEvent
        public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CalamityEntity.ZENITH_PROJECTILE.get(), ZenithProjectile.ZenithProjectileRenderer::new);
            event.registerEntityRenderer(CalamityEntity.METEOR.get(), Meteor.Render::new);
            event.registerEntityRenderer(CalamityEntity.FIRE_METEOR.get(), FireMeteor.Render::new);
            event.registerEntityRenderer(CalamityEntity.ACIDIC_RAIN.get(), AcidicRain.Render::new);
            event.registerEntityRenderer(CalamityEntity.MINI_DRAGON.get(), MiniDragonBorn.Render::new);
            event.registerEntityRenderer(CalamityEntity.SHADOWS_RAIN.get(), ShadowsRain.Render::new);
            event.registerEntityRenderer(CalamityEntity.NEBULA.get(), Nebula.Render::new);
            event.registerEntityRenderer(CalamityEntity.RANCOR.get(), RancorMagicCircle.Render::new);
            event.registerEntityRenderer(CalamityEntity.RANCOR_LASER.get(), RancorLaserBeam.Render::new);
            event.registerEntityRenderer(CalamityEntity.USF.get(), UniverseSplitterField.Render::new);
            event.registerEntityRenderer(CalamityEntity.USB.get(), UniverseSplitterSmallBeam.Render::new);
            event.registerEntityRenderer(CalamityEntity.USH.get(), UniverseSplitterHugeBeam.Render::new);
            event.registerEntityRenderer(CalamityEntity.ETERNITY_HEX.get(), EternityHex.Render::new);
            event.registerEntityRenderer(CalamityEntity.EXCELSUS_BLUE.get(), ExProjectile.Render::new);
            event.registerEntityRenderer(CalamityEntity.EXCELSUS_MAIN.get(), ExProjectile.Render::new);
            event.registerEntityRenderer(CalamityEntity.EXCELSUS_PINK.get(), ExProjectile.Render::new);
            event.registerEntityRenderer(CalamityEntity.NEBULA_CLOUD_CORE.get(), NebulaCloudCore.Render::new);
            event.registerEntityRenderer(CalamityEntity.NEBULA_NOVA.get(), NebulaNova.Render::new);
            event.registerEntityRenderer(CalamityEntity.HEAL_ORB.get(), GladiatorHealOrb.Render::new);
            event.registerEntityRenderer(CalamityEntity.JEWEL_SPIKE.get(), JewelSpike.Render::new);
            event.registerEntityRenderer(CalamityEntity.TESLA_AURA.get(), TeslaAura.Render::new);
            event.registerEntityRenderer(CalamityEntity.LUNAR_FLARE.get(), LunarFlare.Render::new);
            event.registerEntityRenderer(CalamityEntity.BLACK_HOLE.get(), BlackHolePet.Renderer::new);
            event.registerEntityRenderer(CalamityEntity.SUN.get(), StarPet.Renderer::new);
        }

        @SubscribeEvent
        public static void registerKey(RegisterKeyMappingsEvent event) {
            ClientInteraction.build(event);
        }

        @SubscribeEvent
        public static void hudRender(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("rage", new RageHud());
            event.registerAboveAll("sponge", new SpongeHud());
            event.registerAboveAll("adrenaline", new AdrenalineHud());
            event.registerAboveAll("fatigue_slot", new FatigueSlot());
            event.registerAboveAll("fatigue_hud", new FatigueHud());
        }

        @SubscribeEvent
        public static void onBakeModel(ModelEvent.BakingCompleted event) {
            try {
                TransformBakeModel.register(event);
            } catch (Exception e) {
                CalamityCurios.LOGGER.error("An exception occurred during the model modification process and has been reverted back to the default", e);
            }
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Pre event) {
            if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
                PurpleFlames.applySprite(event);
            }
        }

        @SubscribeEvent
        public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            LayerDefinition definition = LayerDefinition.create(HumanoidModel.createMesh(
                new CubeDeformation(0.5f), 0f), 16, 16);
            event.registerLayerDefinition(FrozenRender.LAYER, () -> definition);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            if (addLayerToPlayerSkin(event, "default")) addLayerToPlayerSkin(event, "slim");
        }

        public static boolean addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
            Set<String> skins = event.getSkins();
            for (String skin : skins) {
                if (skin.equals(skinName)) {
                    EntityRenderer<? extends Player> render = event.getSkin(skin);
                    if (render instanceof LivingEntityRenderer entityRenderer) {
                        entityRenderer.addLayer(new FrozenRender(entityRenderer));
                        return false;
                    }
                }
            }

            return true;
        }
    }
//    @SubscribeEvent
//    public static void serializers(final RegisterEvent event) {
//        if (event.getRegistryKey().equals(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS)) {
//            event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper ->
//                helper.register(CalamityCurios.ModResource("global_loot_modifier"), GlobalLootModifier.CODEC));
//        } else if (event.getRegistryKey().equals(Registry.LOOT_ITEM_REGISTRY)) {
//            event.register(Registry.LOOT_ITEM_REGISTRY, CalamityCurios.ModResource("table_type"), () -> LootTableTypeCondition.LOOT_TABLE_ID);
//        }
//    }
}
