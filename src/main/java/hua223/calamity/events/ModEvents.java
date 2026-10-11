package hua223.calamity.events;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import hua223.calamity.integration.curios.item.Mask;
import hua223.calamity.integration.curios.item.PortalSkirt;
import hua223.calamity.integration.curios.item.Sponge;
import hua223.calamity.integration.curios.item.entropy.NihilityShell;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.entity.SummonedAncientKnight;
import hua223.calamity.register.gui.CalamityCurseScreen;
import hua223.calamity.register.gui.HyperplaneMatrixOperatingInterface;
import hua223.calamity.register.rune.SpellType;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.particle.*;
import hua223.calamity.render.font.DradonTechnologyFont;
import hua223.calamity.render.item.TransformBakeModel;
import hua223.calamity.render.entity.PlayerLayerRender;
import hua223.calamity.render.entity.PurpleFlames;
import hua223.calamity.render.hud.*;
import hua223.calamity.render.shaders.CalamityShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.RegisterList.CALAMITY_CURES;
import static hua223.calamity.register.items.CalamityItems.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void addPlayerAttribute(EntityAttributeModificationEvent event) {
        for (Attribute attribute : CalamityAttributes.getAll())
            if (!event.has(EntityType.PLAYER, attribute))
                event.add(EntityType.PLAYER, attribute);
    }

    //Okay, I feel sad about my artistic ability
//    public static void Render(CustomizeGuiOverlayEvent.BossEventProgress event) {
//        event.getBossEvent()
//    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(CalamityCurios.getEntityType(SummonedAncientKnight.class), SummonedAncientKnight.prepareAttributes().build());
    }

    @SubscribeEvent
    public static void onConfigSetUp(ModConfigEvent.Loading event) {
        CalamityConfig.onLoadConfigInfo(event);
    }

    @SubscribeEvent
    public static void onConfigReLoad(ModConfigEvent.Reloading event) {
        CalamityConfig.onLoadConfigInfo(event);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientMod {
        @SubscribeEvent
        @SuppressWarnings("all")
        public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
            MenuScreens.register(CALAMITY_CURES.get(), CalamityCurseScreen::new);
            CuriosRendererRegistry.register(PORTAL_SKIRT.get(), PortalSkirt.Render::new);
            CuriosRendererRegistry.register(NIHILITY_SHELL.get(), NihilityShell.Render::new);

            Supplier<ICurioRenderer> mask = Suppliers.memoize(Mask.Render::new);
            CuriosRendererRegistry.register(DEITY_MASK.get(), mask);
            CuriosRendererRegistry.register(AVATAR_MASK.get(), mask);

            Supplier<ICurioRenderer> barrier = Suppliers.memoize(Sponge.BarrierRenderer::new);
            CuriosRendererRegistry.register(SPONGE.get(), barrier);
            CuriosRendererRegistry.register(ROVER_DRIVE.get(), barrier);
        }

        @SubscribeEvent
        public static void onParticleRegister(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleRegister.ETERNITY_DUST.get(), EternityDust.DustProvider::new);
            event.registerSpriteSet(ParticleRegister.SAKURA.get(), Sakura.Provider::new);
            event.registerSpriteSet(ParticleRegister.PULSE.get(), Pulse.PulseProvider::new);
            event.registerSpriteSet(ParticleRegister.SPARK.get(), SparkParticle.SparkProvider::new);
            event.registerSpriteSet(ParticleRegister.GLOW_SPARK.get(),  GlowSparkParticle.GlowSparkProvider::new);
            event.registerSpriteSet(ParticleRegister.POINT.get(), PointParticle.PointProvider::new);
            event.registerSpriteSet(ParticleRegister.BLOOD.get(), Blood.Provider::new);
            event.registerSpriteSet(ParticleRegister.TOTEM.get(), spriteSet -> new ColorfulTotemType.ColorfulTotemProvider());
            event.registerSpriteSet(ParticleRegister.ELECTRIC_EXPLOSION_RING.get(), ElectricExplosionRing.Provider::new);
            event.registerSpriteSet(ParticleRegister.STORM_LIGHTNING.get(), set -> new StormLightning.Provider());
            event.registerSpriteSet(ParticleRegister.BALLISTIC_POISON_CLOUD.get(), BallisticPoisonCloud.Provider::new);
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Post event) {
            TextureAtlas atlas = event.getAtlas();
            ResourceLocation location = atlas.location();
            if (location.equals(InventoryMenu.BLOCK_ATLAS))
                PurpleFlames.afterSpriteStitch(atlas);
            else if (location.getNamespace().equals(MODID)) {
                AdrenalineHud.afterMainTextureLoad(atlas);
                RageHud.afterMainTextureLoad(atlas);
                EnergyBarHud.afterMainTextureLoad(atlas);
                BarrierHud.afterMainTextureLoad(atlas);
                FatigueHud.afterMainTextureLoad(atlas);
                CrystallizationRenderLayer.afterMainTextureLoad(atlas);
                CalamityCurseScreen.afterMainTextureLoad(atlas);
                SpellType.afterMainTextureLoad(atlas);
                FatigueSlot.afterMainTextureLoad(atlas);
                DradonTechnologyFont.afterMainTextureLoad(atlas);
                HyperplaneMatrixOperatingInterface.OptionsUI.afterMainTextureLoad(atlas);
            }
        }

        @SubscribeEvent
        public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
            ItemProperties.register(CalamityItems.NEBULOUS_CATACLYSM.get(), CalamityCurios.ModResource("cataclysm_hold"),
                (itemStack, clientLevel, livingEntity, i) ->
                    TransformBakeModel.modelSelectionPredicate(itemStack, livingEntity) ? 1f : 0f);

            event.registerReloadListener(new TextureAtlasHolder(Minecraft.getInstance().getTextureManager(),
                CalamityCurios.ModResource("textures/atlas/calamity_gui.png"), CalamityCurios.ModResource("calamity_gui")) {});
        }

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) {
            CalamityShaders.registerShaders(event);
        }

        @SubscribeEvent
        public static void registerKey(RegisterKeyMappingsEvent event) {
            ClientInteraction.build(event);
        }

        @SubscribeEvent
        public static void hudRender(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("rage", RageHud.INSTANCE);
            event.registerAboveAll("adrenaline", AdrenalineHud.INSTANCE);
            event.registerAboveAll("sponge", new BarrierHud());
            event.registerAboveAll("fatigue_slot", new FatigueSlot());
            event.registerAboveAll("fatigue_hud", new FatigueHud());
            event.registerAboveAll("crystallization", new CrystallizationRenderLayer());
        }

        @SubscribeEvent
        public static void onBakeModel(ModelEvent.ModifyBakingResult event) {
            CalamityCurios.LOGGER.info("Start replacing the model");
            TransformBakeModel.register(event);
        }

        @SubscribeEvent
        public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            final MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.5f), 0f);
            event.registerLayerDefinition(PlayerLayerRender.getLayerLocation("frozen_layer"), () -> LayerDefinition.create(mesh, 16, 16));
            event.registerLayerDefinition(PlayerLayerRender.getLayerLocation("crescent_stance"), () -> LayerDefinition.create(mesh, 64, 32));
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            addLayerToPlayerSkin(event, "default");
            addLayerToPlayerSkin(event, "slim");
        }

        @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
        public static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
            EntityRenderer<? extends Player> render = event.getSkin(skinName);
            if (render instanceof LivingEntityRenderer entityRenderer)
                entityRenderer.addLayer(new PlayerLayerRender(entityRenderer));
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
