package hua223.calamity.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
//My previous knowledge of shaders was only in the hints for game loading. I don't even know what it is. this is too bad
public class CalamityShaders extends RenderType {
    public static final RenderTarget MAIN = Minecraft.getInstance().getMainRenderTarget();
    //Textures
    public static final ResourceLocation BASE_TEXTURE = CalamityCurios.ModResource("textures/effect/invisible_pixel.png");
    public static final ResourceLocation FIRE_NOISE = CalamityCurios.ModResource("textures/effect/fire_noise.png");
    public static final ResourceLocation BLOOM_CIRCLE_SMALL = CalamityCurios.ModResource("textures/effect/bloom_circle_small.png");
    public static final ResourceLocation WAVY_BLOTCH_NOISE = CalamityCurios.ModResource("textures/effect/wavy_blotch_noise.png");
    public static final ResourceLocation PSYCHEDELIC = CalamityCurios.ModResource("textures/effect/psychedelic.png");
    public static final ResourceLocation DENDRITIC_NOISE = CalamityCurios.ModResource("textures/effect/dendritic_noise.png");
    public static final ResourceLocation PERLIN = CalamityCurios.ModResource("textures/effect/perlin.png");
    public static final ResourceLocation GRAINY_NOISE = CalamityCurios.ModResource("textures/effect/grainy_noise.png");
    public static final ResourceLocation CRACKED_NOISE = CalamityCurios.ModResource("textures/effect/cracked_noise.png");
    public static final ResourceLocation COSMOS = CalamityCurios.ModResource("textures/effect/cosmos.png");
    public static final ResourceLocation TECHY_NOISE = CalamityCurios.ModResource("textures/effect/techy_noise.png");
    public static final ResourceLocation GRAYSCALE_WATER = CalamityCurios.ModResource("textures/effect/grayscale_water.png");
    public static final ResourceLocation PERLIN2 = CalamityCurios.ModResource("textures/effect/perlin_2.png");

    //Shaders
    private static Supplier<ShaderInstance> BASE_SHADER;
    private static Supplier<ShaderInstance> SCARLET_LIGHTNING_SHADER;
    private static Supplier<ShaderInstance> NAMELESS_DEITY_FONT_SHADER;
    private static Supplier<ShaderInstance> DEMON_SHADE_FONT;
    private static ShaderInstance BLACK_HOLE_INSTANCE;
    private static ShaderStateShard FADED_UV_MAP_STREAK_STATE_SHARD;
    private static ShaderStateShard FLAME_STATE_SHARD;
    private static ShaderStateShard RANCOR_STATE_SHARD;
    private static ShaderStateShard BLACK_HOLE_SHARD;
    private static ShaderStateShard PORTAL_SKIRT_SHARD;
    private static ShaderStateShard BASE_SHARD;
    private static ShaderStateShard RADIAL_SHINE;
    private static ShaderStateShard DISINTEGRATION;
    private static ShaderStateShard SUPER_COSMIC;
    private static ShaderStateShard ENERGY;
    private static ShaderStateShard STATIC_OVERLAY;
    private static ShaderStateShard SUN_SHARD;
    private static ShaderStateShard HOLOGRAPHIC_FLOW;
    private static ShaderStateShard CALAMITY_BACKGROUND;
    private static ShaderStateShard VOLUMETRIC_SPRAY;
    private static ShaderStateShard DRAEDON_BARRIER;

    //Blend Func
    public static final TransparencyStateShard XNA_ADDITIVE_BLEND = RenderStateShard.LIGHTNING_TRANSPARENCY;
    public static final TransparencyStateShard XNA_ALPHA_BLEND = new TransparencyStateShard("color_blend_alpha_write", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    private CalamityShaders(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling,
                    boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
    
    public static void registerShaders(RegisterShadersEvent event) {
        //Complete
        if (BASE_SHADER != null) return;

        try {
            Consumer<ShaderInstance> empty = shader -> {};

            ResourceProvider provider = event.getResourceProvider();

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("faded_uv_map_streak"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader ->
                FADED_UV_MAP_STREAK_STATE_SHARD = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("flame"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader ->
                FLAME_STATE_SHARD = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("rancor_magic_circle"),
                DefaultVertexFormat.NEW_ENTITY), shader ->
                RANCOR_STATE_SHARD = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("base"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> BASE_SHARD = new ShaderStateShard(BASE_SHADER = () -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("disintegration"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> DISINTEGRATION = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("real_black_hole"),
                DefaultVertexFormat.POSITION_TEX), shader -> {
                BLACK_HOLE_INSTANCE = shader;
                BLACK_HOLE_SHARD = new ShaderStateShard(() -> shader);
            });

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("radial_shine"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> RADIAL_SHINE = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("sun"),
                DefaultVertexFormat.POSITION_TEX), shader -> SUN_SHARD = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("converging_genesis_energy"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> ENERGY = new ShaderStateShard(() -> shader));

            event.registerShader(new ErosionShader(provider), empty);

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("lightning_arc"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> SCARLET_LIGHTNING_SHADER = () -> shader);

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("dark_portal"),
                DefaultVertexFormat.POSITION_TEX), shader -> PORTAL_SKIRT_SHARD = new ShaderStateShard(() -> shader));

            event.registerShader(new StaticOverlayShader(provider), shader -> STATIC_OVERLAY = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("nameless_deity_font"),
                DefaultVertexFormat.POSITION_COLOR_TEX), shader -> NAMELESS_DEITY_FONT_SHADER = () -> shader);

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("super_cosmic"),
                DefaultVertexFormat.POSITION_TEX), shader -> SUPER_COSMIC = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("holographic_flow"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> HOLOGRAPHIC_FLOW = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("calamity_bg"),
                DefaultVertexFormat.POSITION_COLOR_TEX), shader -> CALAMITY_BACKGROUND = new ShaderStateShard(() -> shader));

            event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("demon_shade_font"),
                DefaultVertexFormat.POSITION_COLOR_TEX), shader -> DEMON_SHADE_FONT = () -> shader);

            event.registerShader(new VolumetricSprayShader(provider), shader -> VOLUMETRIC_SPRAY = new ShaderStateShard(() -> shader));

            event.registerShader(new DraedonBarrierRenderer(provider), shader -> DRAEDON_BARRIER = new ShaderStateShard(() -> shader));

            TextureManager manager = Minecraft.getInstance().textureManager;
            CalamityHeavenlyRealmShader.init(event, provider, manager);
            setLinearWrap(manager, FIRE_NOISE);
            setLinearWrap(manager, BLOOM_CIRCLE_SMALL);
            setLinearWrap(manager, WAVY_BLOTCH_NOISE);
            setLinearWrap(manager, PSYCHEDELIC);
            setLinearWrap(manager, PERLIN);
            setLinearWrap(manager, COSMOS);
            setLinearWrap(manager, TECHY_NOISE);
            setLinearWrap(manager, CalamityCurios.ModResource("textures/misc/code_picture.png"));
            setLinearWrap(manager, GRAYSCALE_WATER);
            setPointWarp(manager, GRAINY_NOISE);
            setLinearWrap(manager, PERLIN2);
            manager.getTexture(CalamityCurios.ModResource("textures/misc/xeroc_eye.png")).setFilter(true, false);
            GlStateManager._bindTexture(0);
        } catch (Exception e) {
            throw new RuntimeException("a fatal error occurred when registering shaders", e);
        }
    }

    public static RenderType getVolumetricSpray() {
        return RenderType.create("volumetric_spray", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
                .setShaderState(VOLUMETRIC_SPRAY)
                .setTransparencyState(XNA_ALPHA_BLEND)       // 预乘混合
                .setCullState(RenderStateShard.CULL)          // 留正面，作为射线入口
                .setTextureState(new MultiTextureStateShard.Builder()
                    .add(PERLIN, false, false)
                    .add(GRAINY_NOISE, false, false).build())
                .createCompositeState(false));
    }

    public static void setLinearWrap(TextureManager manager, ResourceLocation texture) {
        setLinearWrap(manager.getTexture(texture).getId());
    }

    //
    public static void setLinearWrap(int id) {
        GlStateManager._bindTexture(id);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

//        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
//        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
//        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
//        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR_MIPMAP_LINEAR);
    }

    public static void setPointWarp(TextureManager manager, ResourceLocation texture) {
        GlStateManager._bindTexture(manager.getTexture(texture).getId());
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
    }

    public static Supplier<ShaderInstance> getBaseShader() {
        return BASE_SHADER;
    }

    public static Supplier<ShaderInstance> getDemonShadeFont() {
        return DEMON_SHADE_FONT;
    }

    @SuppressWarnings("deprecation")
    public static final ParticleRenderType GENERIC_BLOOM = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            XNA_ADDITIVE_BLEND.setupRenderState();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "GENERIC_BLOOM";
        }
    };

    public static Supplier<ShaderInstance> getScarletLightningShader() {
        return SCARLET_LIGHTNING_SHADER;
    }

    public static Supplier<ShaderInstance> getNamelessDeityFontShader() {
        return NAMELESS_DEITY_FONT_SHADER;
    }

    public static RenderType getLemniscateRenderType(ResourceLocation texture) {
        return RenderType.create("lemniscate", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
            256, false, false, CompositeState.builder()
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setCullState(RenderStateShard.NO_CULL)
                .setShaderState(FADED_UV_MAP_STREAK_STATE_SHARD)
                .setTextureState(new TextureStateShard(texture, false, true))
                .createCompositeState(false)
        );
    }

    public static RenderType getPortalSkirt() {
        return RenderType.create("portal_skirt", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
            64, false, true, CompositeState.builder()
                .setShaderState(PORTAL_SKIRT_SHARD)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setCullState(RenderStateShard.NO_CULL)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setTextureState(new RenderStateShard.MultiTextureStateShard.Builder()
                    .add(CalamityCurios.ModResource("textures/effect/rift_inner.png"), false, false)
                    .add(WAVY_BLOTCH_NOISE, false, false).build()).createCompositeState(false)
        );
    }

    public static RenderType getStaticOverlay(ResourceLocation location, boolean fabulous) {
        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
            .setOverlayState(NO_OVERLAY)
            .setLightmapState(LIGHTMAP)
            .setShaderState(STATIC_OVERLAY)
            .setTransparencyState(XNA_ALPHA_BLEND)
            .setTextureState(new RenderStateShard.MultiTextureStateShard.Builder()
                .add(location, false, false)
                .add(CalamityShaders.GRAINY_NOISE, false, false).build());

        if (fabulous) builder.setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE);
        return RenderType.create("static_overlay", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, true, true, builder.createCompositeState(true));
    }

    public static ShaderInstance getBlackHoleInstance() {
        return BLACK_HOLE_INSTANCE;
    }

    public static RenderType getBlackHole() {
        return RenderType.create("black_hole", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
            32, false, false, CompositeState.builder()
                .setShaderState(BLACK_HOLE_SHARD)
                .setTextureState(new MultiTextureStateShard.Builder()
                    .add(CalamityShaders.BASE_TEXTURE, false, false)
                    .add(CalamityShaders.FIRE_NOISE, false, false).build())
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setCullState(RenderStateShard.CULL)
                .createCompositeState(false)
        );
    }

    public static RenderType getDisintegration() {
        return RenderType.create("disintegration", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            256, false, false, CompositeState.builder()
                .setTextureState(new TextureStateShard(CalamityShaders.PERLIN, false, false))
                .setShaderState(DISINTEGRATION)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .createCompositeState(false));
    }

    public static RenderType getSuperCosmic() {
        return RenderType.create("super_cosmic", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
            64, false, false, CompositeState.builder()
                .setShaderState(SUPER_COSMIC)
                .setWriteMaskState(COLOR_WRITE)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setTextureState(new RenderStateShard.MultiTextureStateShard.Builder()
                    .add(CalamityShaders.COSMOS, false, false)
                    .add(CalamityShaders.CRACKED_NOISE, false, false)
                    .build()).createCompositeState(false));
    }

    public static RenderType getRancorLaserRenderType(ResourceLocation texture) {
        return RenderType.create("flame_shader", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
            256, false, false, CompositeState.builder()
                .setShaderState(FLAME_STATE_SHARD)
                .setCullState(RenderStateShard.NO_CULL)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setTextureState(new TextureStateShard(texture, false, true))
                .createCompositeState(false)
        );
    }

    public static RenderType getDraedonBarrierRenderType() {
        return RenderType.create("barrier", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            256, false, false, CompositeState.builder()
                .setShaderState(DRAEDON_BARRIER)
                .setCullState(RenderStateShard.NO_CULL)
                .setTransparencyState(XNA_ADDITIVE_BLEND)
                .setTextureState(new TextureStateShard(TECHY_NOISE, false, false))
                .createCompositeState(false)
        );
    }

    public static RenderType getRancorCircleRenderType(ResourceLocation texture, boolean isGlowMask, boolean noDepthWrite) {
        return RenderType.create("rancor_magic_circle", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, false, false, CompositeState.builder()
                .setShaderState(RANCOR_STATE_SHARD)
                .setTextureState(new TextureStateShard(texture, false, true))
                .setTransparencyState(isGlowMask ? XNA_ADDITIVE_BLEND : XNA_ALPHA_BLEND)
                .setOverlayState(RenderStateShard.NO_OVERLAY)
                .setCullState(RenderStateShard.NO_CULL)
                .setWriteMaskState(noDepthWrite ? RenderStateShard.COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(false)
        );
    }

    public static RenderType getHolographicFlow() {
        return RenderType.create("holographic_flow", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
            256, false, false, CompositeState.builder()
                .setShaderState(HOLOGRAPHIC_FLOW)
                .setDepthTestState(NO_DEPTH_TEST)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setTextureState(new RenderStateShard.MultiTextureStateShard.Builder()
                    .add(TECHY_NOISE, false, false)
                    .add(CalamityCurios.ModResource("textures/misc/code_picture.png"), false, false).build())
                .createCompositeState(false)
        );
    }

    public static RenderType getConvergingGenesisEnergy() {
        return RenderType.create("converging_genesis_energy", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.TRIANGLES, 256, false, false, CompositeState.builder()
                .setShaderState(ENERGY)
                .setTextureState(new TextureStateShard(CalamityShaders.PERLIN, false, false))
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderStateShard.CULL)
                .createCompositeState(false)
        );
    }

    public static RenderType getGlowRenderType(ResourceLocation texture) {
        return RenderType.create("glow_entity", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, false, true, CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(XNA_ADDITIVE_BLEND)
                .setWriteMaskState(COLOR_WRITE)//
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(false));
    }

    public static RenderType getStormMaidensGlow() {
        return create("storm_maidens", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, true, true, CompositeState.builder()
                .setCullState(NO_CULL)
                .setOverlayState(OVERLAY)
                .setLightmapState(LIGHTMAP)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST).createCompositeState(true));
    }

    public static RenderType getCalamityBG() {
        return create("calamity_bg", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            128, false, true, CompositeState.builder()
                .setShaderState(CALAMITY_BACKGROUND)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setTextureState(new TextureStateShard(GRAYSCALE_WATER, false, false))
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST).createCompositeState(false));
    }

    public static RenderType getEnchanmentRenderType(ResourceLocation texture) {
        return RenderType.create("enchanted_particle", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            256, false, false, CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                .createCompositeState(false));
    }

    public static RenderType getRadialShineRenderType() {
        return RenderType.create("radial_shine", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            32, false, false, CompositeState.builder()
                .setShaderState(RADIAL_SHINE)
                .setTransparencyState(XNA_ALPHA_BLEND)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setTextureState(new TextureStateShard(CalamityShaders.WAVY_BLOTCH_NOISE, false, false))
                .createCompositeState(false));
    }

    public static RenderType getSunRenderType() {
        return RenderType.create("sun", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
            32, false, false, CompositeState.builder()
                .setShaderState(SUN_SHARD)
                .setTextureState(new MultiTextureStateShard.Builder()
                    .add(CalamityShaders.DENDRITIC_NOISE, false, false)
                    .add(CalamityShaders.WAVY_BLOTCH_NOISE, false, false)
                    .add(CalamityShaders.PSYCHEDELIC, false, false).build())
                .setTransparencyState(XNA_ALPHA_BLEND)
                .createCompositeState(false));
    }

    public static RenderType getCircleSmall() {
        return RenderType.create("circleSmall", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            32, false, false, CompositeState.builder()
                .setShaderState(BASE_SHARD)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setTextureState(new TextureStateShard(CalamityShaders.BLOOM_CIRCLE_SMALL, false, false))
                .setTransparencyState(XNA_ALPHA_BLEND).createCompositeState(false));
    }

    public static class CalamityShaderInstance extends ShaderInstance {
        protected CalamityShaderInstance(ResourceProvider provider, ResourceLocation shaderLocation, VertexFormat format) throws IOException {
            super(provider, shaderLocation, format);
        }
    }
}
