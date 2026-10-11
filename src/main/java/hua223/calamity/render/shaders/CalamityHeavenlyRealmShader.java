package hua223.calamity.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.screen.particleset.HeavenlyRealmSmoke;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static hua223.calamity.render.shaders.CalamityShaders.MAIN;

@OnlyIn(Dist.CLIENT)
public class CalamityHeavenlyRealmShader {
    private static float tick;
    private static byte fadeState;
    private static HeavenlyRealmSmoke smokes;
    private static float differentStarsInterpolant;
    private static float currentStarsInterpolant;
    private static float lastStarsInterpolant;

    private static int FOG_TEXTURE;
    private static int BASE_TEXTURE;
    private static int MOLTEN_NOISE_TEXTURE;
    private static int KALI_SET_FRACTAL_TEXTURE;
    private static int TURBULENT_NOISE_TEXTURE_TEXTURE;
    private static RenderTarget HEAVENLY_REALM_TARGET;
    private static Supplier<ShaderInstance> GALAXY_SHADER;
    private static Supplier<ShaderInstance> HEAVENLY_REALM_BG_SHADER;

    public static void drawHeavenlyRealmBackground(Matrix4f matrix, int x1, int y1, int x2, int y2, int z) {
        HEAVENLY_REALM_TARGET.clear(Minecraft.ON_OSX);
        HEAVENLY_REALM_TARGET.bindWrite(false);
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE.value);
        RenderSystem.backupProjectionMatrix();
        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        mv.last().pose().identity().translation(0.0F, 0.0F, -2000.0F);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(RenderUtil.TRANSIENT_MATRIX.setOrtho(0.0F,
            HEAVENLY_REALM_TARGET.width, HEAVENLY_REALM_TARGET.height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);

        runTick();
        drawBaseOverlay();
        drawBackground(Math.max(HEAVENLY_REALM_TARGET.width, HEAVENLY_REALM_TARGET.height));
        drawSmoke();
        drawGalaxies();

        mv.popPose();
        MAIN.bindWrite(false);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.value,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SourceFactor.ZERO.value, GlStateManager.DestFactor.ONE.value);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, HEAVENLY_REALM_TARGET.getColorTextureId());
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x1, y1, z).uv(0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z).uv(1, 1).endVertex();
        builder.vertex(matrix, x2, y2, z).uv(1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z).uv(0, 0).endVertex();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.defaultBlendFunc();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);
    }

    private static void drawBaseOverlay() {
        // Draw the sky overlay.
        RenderSystem.setShader(CalamityShaders.getBaseShader());
        RenderSystem.setShaderTexture(0, BASE_TEXTURE);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        float tint = 88f / 255f * 0.76f;
        builder.vertex(0, HEAVENLY_REALM_TARGET.height, 0).color(tint, tint, tint, 1f).uv(0f, 1f).endVertex();
        builder.vertex(HEAVENLY_REALM_TARGET.width, HEAVENLY_REALM_TARGET.height, 0).color(tint, tint, tint, 1f).uv(1f, 1f).endVertex();
        builder.vertex(HEAVENLY_REALM_TARGET.width, 0, 0).color(tint, tint, tint, 1f).uv(1f, 0f).endVertex();
        builder.vertex(0, 0, 0).color(tint, tint, tint, 1f).uv(0f, 0f).endVertex();

        BufferUploader.drawWithShader(builder.end());
    }

    private static void drawBackground(float areaComponent) {
        float interpolant = Mth.lerp(Minecraft.getInstance().getPartialTick(), lastStarsInterpolant, currentStarsInterpolant);
        int frontStarColor = ARGB32.lerp(interpolant, 0xFFFF7F50, 0xFFFFFFFF);
        int backStarColor = ARGB32.lerp(interpolant, 0xFFFFFF00, 0xFFFFFEFE);
        ShaderInstance instance = HEAVENLY_REALM_BG_SHADER.get();
        setColorUniform(instance.safeGetUniform("FrontStarColor"), frontStarColor, .5f);
        setColorUniform(instance.safeGetUniform("BackStarColor"), backStarColor, .4f);
        RenderSystem.setShader(HEAVENLY_REALM_BG_SHADER);
        RenderSystem.setShaderTexture(0, KALI_SET_FRACTAL_TEXTURE);
        RenderSystem.setShaderTexture(1, TURBULENT_NOISE_TEXTURE_TEXTURE);

        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        builder.vertex(0, areaComponent, 0).color(.4f, .4f, .4f, .4f).uv(0f, 1f).endVertex();
        builder.vertex(areaComponent, areaComponent, 0).color(.4f, .4f, .4f, .4f).uv(1f, 1f).endVertex();
        builder.vertex(areaComponent, 0, 0).color(.4f, .4f, .4f, .4f).uv(1f, 0f).endVertex();
        builder.vertex(0, 0, 0).color(.4f, .4f, .4f, .4f).uv(0f, 0f).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static void drawSmoke() {
        // Draw all active smoke particles in the background.
        RenderSystem.setShaderTexture(0, FOG_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);

        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (HeavenlyRealmSmoke.BackgroundSmoke particle : smokes.getParticles())
            particle.baseDraw(builder);

        BufferUploader.drawWithShader(builder.end());
    }

    private static void drawGalaxies() {
        // Draw galaxies in the sky.
        int tries = 0;
        int[] galaxyColor1 = new int[3];
        int[] galaxyColor2 = new int[3];
        long[] seed = new long[] {422L};
        List<Vector2f> galaxyPositions = new ArrayList<>(30);

        RenderSystem.setShader(GALAXY_SHADER);
        RenderSystem.setShaderTexture(0, MOLTEN_NOISE_TEXTURE);
        ShaderInstance galaxyShader = GALAXY_SHADER.get();
        AbstractUniform uniform = galaxyShader.safeGetUniform("Transformation");
        Vector2f scalingFactor = new Vector2f(HEAVENLY_REALM_TARGET.width / 2560f, HEAVENLY_REALM_TARGET.height / 1440f);

        loop: for (int i = 0; i < 30; i++) {
            // Randomly decide the orientation of galaxies.
            uniform.set(Mth.lerp(promoteWithSeeds(seed), 0.7f, 1.3f),
                Mth.lerp(promoteWithSeeds(seed), 0.2f, 0.3f),
                Mth.lerp(promoteWithSeeds(seed), -0.3f, 0.3f),
                Mth.lerp(promoteWithSeeds(seed), 0.7f, 1.3f));

            float baseGalaxyScale = Mth.lerp(promoteWithSeeds(seed), 0.09f, 0.61f);

            // Randomly place galaxies throughout the sky. They attempt to avoid each other. If these attempts fail, the process is immediately terminated.
            Vector2f galaxySpawnPosition = new Vector2f(Mth.lerp(promoteWithSeeds(seed), 0, HEAVENLY_REALM_TARGET.width + 500),
                Mth.lerp(promoteWithSeeds(seed), 0, HEAVENLY_REALM_TARGET.height + 300)).mul(scalingFactor);

            for (Vector2f g : galaxyPositions) {
                if (g.distanceSquared(galaxySpawnPosition) < 160f * 160f) {
                    i--;
                    tries++;
                    if (tries >= 30) break loop;
                    continue loop;
                }
            }

            // Randomly decide galaxy colors based on the seed.
            float hue = promoteWithSeeds(seed);
            float distanceFadeOut = Mth.clampedMap(baseGalaxyScale, 0.4f, 0.9f, 0.2f, 1f);
            hslToRgb(galaxyColor1, hue, 1f, 0.67f, distanceFadeOut);
            hslToRgb(galaxyColor2, (hue + 0.11f) % 1f, 1f, 0.67f, distanceFadeOut);
            galaxyColor1[1] /= 2;
            galaxyColor2[1] /= 3;
            int a = (int) (255 * distanceFadeOut);

            tries = 0;
            galaxyPositions.add(galaxySpawnPosition);
            BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            buildGalaxyVertex(RenderUtil.TRANSIENT_MATRIX.identity().translate(galaxySpawnPosition.x, galaxySpawnPosition.y, 0f), builder, galaxyColor1, a);
            buildGalaxyVertex(RenderUtil.TRANSIENT_MATRIX.scale(.8f), builder, galaxyColor1, a);
            BufferUploader.drawWithShader(builder.end());
        }
    }

    private static void buildGalaxyVertex(Matrix4f matrix4f, BufferBuilder builder, int[] galaxyColor, int alpha) {
        builder.vertex(matrix4f, -380, 380, 0).color(galaxyColor[0], galaxyColor[1], galaxyColor[2], alpha).uv(0f, 1f).endVertex();
        builder.vertex(matrix4f, 380, 380, 0).color(galaxyColor[0], galaxyColor[1], galaxyColor[2], alpha).uv(1f, 1f).endVertex();
        builder.vertex(matrix4f, 380, -380, 0).color(galaxyColor[0], galaxyColor[1], galaxyColor[2], alpha).uv(1f, 0f).endVertex();
        builder.vertex(matrix4f, -380, -380, 0).color(galaxyColor[0], galaxyColor[1], galaxyColor[2], alpha).uv(0f, 0f).endVertex();
    }

    //Random number algorithm of Random class，removed the last disturbance,
    //And the promotion of seeds and the avoidance of object creation
    private static float promoteWithSeeds(long[] seed) {
        return (float) ((seed[0] = (seed[0] * 25214903917L + 11) & 0xFFFFFFFFFFFFL) >> 48 - 24) / 16777216f;
    }

    public static void hslToRgb(int[] color, float hue, float saturation, float luminosity, float fade) {
        if (saturation == 0f) {
            color[0] = (int) (Math.round(luminosity * 255.0) * fade);
            color[1] = (int) (Math.round(luminosity * 255.0) * fade);
            color[2] = (int) (Math.round(luminosity * 255.0) * fade);
        } else {
            double d2 = !(luminosity < 0.5) ? (luminosity + saturation - luminosity * saturation) : (luminosity * (1.0 + saturation));
            double t = 2.0 * luminosity - d2;
            double d3 = hue + 0.33333333333333331;
            double c = hue - 0.33333333333333331;
            color[0] = (int) (Math.round(hue2rgb(d3, t, d2) * 255.0) * fade);
            color[1] = (int) (Math.round(hue2rgb(hue, t, d2) * 255.0) * fade);
            color[2] = (int) (Math.round(hue2rgb(c, t, d2) * 255.0) * fade);
        }
    }

    public static double hue2rgb(double c, double t1, double t2) {
        if (c < 0.0) c += 1.0;
        else if (c > 1.0) c -= 1.0;

        if (6.0 * c < 1.0)return t1 + (t2 - t1) * 6.0 * c;
        if (2.0 * c < 1.0) return t2;
        if (3.0 * c < 2.0) return t1 + (t2 - t1) * (2.0 / 3.0 - c) * 6.0;
        return t1;
    }

    private static void setColorUniform(AbstractUniform uniform, int color, float scale) {
        uniform.set(ARGB32.red(color) / 255f * scale, ARGB32.green(color) / 255f * scale, ARGB32.blue(color) / 255f * scale);
    }

    private static void runTick() {
        if (smokes == null) {
            smokes = new HeavenlyRealmSmoke();
            DelayRunnable.addUniqueLoopTask(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player == null) {
                    smokes = null;
                    return true;
                } else if (smokes.recycleTimer == 10) {
                    //If it is still in the GUI, please do not close it as it may be rendered again at any time
                    if (minecraft.screen == null) {
                        smokes = null;
                        return true;
                    } else smokes.recycleTimer = 5;
                }

                return false;
            }, 10, CalamityHeavenlyRealmShader.class);
        }

        if (tick != RenderUtil.getLocalTick()) {
            smokes.update();
            tick = RenderUtil.getLocalTick();
            switch (fadeState) {
                case 0 -> {
                    if (Math.random() < .005f) {
                        differentStarsInterpolant = .45f + (float) Math.random() * .55f;
                        fadeState = 1;
                    }
                }

                case 1 -> {
                    lastStarsInterpolant = currentStarsInterpolant;
                    currentStarsInterpolant = Math.min(differentStarsInterpolant, currentStarsInterpolant + .01f);
                    if (lastStarsInterpolant == differentStarsInterpolant) fadeState = 2;
                }

                case 2 -> {
                    lastStarsInterpolant = currentStarsInterpolant;
                    currentStarsInterpolant = Math.max(0, currentStarsInterpolant - .01f);
                    if (lastStarsInterpolant == 0) fadeState = 0;
                }
            }
        }
    }

    public static void init(RegisterShadersEvent event, ResourceProvider provider, TextureManager manager) throws IOException {
        event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("heavenly_realm_bg"),
            DefaultVertexFormat.POSITION_COLOR_TEX), shader -> HEAVENLY_REALM_BG_SHADER = () -> shader);

        event.registerShader(new ShaderInstance(provider, CalamityCurios.ModResource("galaxy"),
            DefaultVertexFormat.POSITION_COLOR_TEX), shader -> GALAXY_SHADER = () -> shader);

        FOG_TEXTURE = manager.getTexture(CalamityCurios.ModResource("textures/effect/fog.png")).getId();
        BASE_TEXTURE = manager.getTexture(CalamityCurios.ModResource("textures/effect/heavenly_realm_base.png")).getId();
        MOLTEN_NOISE_TEXTURE = manager.getTexture(CalamityCurios.ModResource("textures/effect/molten_noise.png")).getId();
        TURBULENT_NOISE_TEXTURE_TEXTURE = manager.getTexture(CalamityCurios.ModResource("textures/effect/turbulent_noise.png")).getId();
        CalamityShaders.setLinearWrap(TURBULENT_NOISE_TEXTURE_TEXTURE);

        //Addition mixing must be black, otherwise everything will be invalid
        HEAVENLY_REALM_TARGET = new TextureTarget(MAIN.width, MAIN.height, false, Minecraft.ON_OSX);
        HEAVENLY_REALM_TARGET.setClearColor(0, 0, 0, 0);

        //No need for frame buffering, it's just a color texture
        KALI_SET_FRACTAL_TEXTURE = GlStateManager._genTexture();
        GlStateManager._bindTexture(KALI_SET_FRACTAL_TEXTURE);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_LINEAR);
        MAIN.bindWrite(false);

        int width = 3072;
        int height = 3072;
        //Well, don't let it block for too long, especially on the main thread
        CompletableFuture.supplyAsync(() -> {
            float[] kaliSetData = new float[width * height];
            //final Vector2f juliaVector = new Vector2f(0.584f, 0.584f);
            IntStream.range(0, height).parallel().forEach(y -> {
                float py0 = y / (float) height - 0.5f;
                for (int i = 0; i < width; i++) {
                    float previousDistance = 0f;
                    float totalChange = 0f;
                    float px = i / (float) width - 0.5f;
                    float py = py0;

                    for (int j = 0; j < 14; j++) {
                        float lenSq = px * px + py * py;
                        px = Math.abs(px) / lenSq - 0.584f;
                        py = Math.abs(py) / lenSq - 0.584f;
                        float distance = Vector2f.length(px, py);
                        totalChange += Math.abs(distance - previousDistance);
                        previousDistance = distance;
                    }

                    kaliSetData[y * width + i] = Float.isNaN(totalChange) || Float.isInfinite(totalChange) || totalChange > 1000f ? 1000f : totalChange;
                }
            });

            return kaliSetData;
        }, Util.backgroundExecutor()).thenAccept(kaliSetData -> RenderSystem.recordRenderCall(() -> {
            GlStateManager._bindTexture(KALI_SET_FRACTAL_TEXTURE);
            FloatBuffer buf = MemoryUtil.memAllocFloat(kaliSetData.length).put(kaliSetData).flip();
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_R32F, width, height, 0, GL30.GL_RED, GL30.GL_FLOAT, buf);
            GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);
            MemoryUtil.memFree(buf);
            MAIN.bindWrite(false);
        }));
    }
}
