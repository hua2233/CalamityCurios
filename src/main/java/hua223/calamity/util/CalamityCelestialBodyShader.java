package hua223.calamity.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class CalamityCelestialBodyShader {
    public static final ResourceLocation BASE_TEXTURE = CalamityCurios.ModResource("textures/effect/invisible_pixel.png");
    public static final ResourceLocation FIRE_NOISE = CalamityCurios.ModResource("textures/effect/fire_noise.png");
    public static final ResourceLocation BLOOM_CIRCLE_SMALL = CalamityCurios.ModResource("textures/effect/bloom_circle_small.png");
    public static final ResourceLocation WAVY_BLOTCH_NOISE = CalamityCurios.ModResource("textures/effect/wavy_blotch_noise.png");
    public static final ResourceLocation PSYCHEDELIC = CalamityCurios.ModResource("textures/effect/psychedelic.png");
    public static final ResourceLocation DENDRITIC_NOISE = CalamityCurios.ModResource("textures/effect/dendritic_noise.png");

    private static BlackHoleShader BLACK_HOLE_INSTANCE;
    private static SunShader SUN_INSTANCE;

    public static class BlackHoleShader extends ShaderInstance {
        private final Uniform BLACK_HOLE_RADIUS;
        private final Uniform ACCRETION_DISK_RADIUS;
        private final Uniform ASPECT_RATIO_CORRECTION_FACTOR;
        private final Uniform CAMERA_ANGLE;
        private final Uniform ZOOM;
        private final Uniform CAMERA_ROTATION_AXIS;
        private final Uniform BLACK_HOLE_CENTER;
        private final Uniform ACCRETION_DISK_COLOR;
        private final Uniform ACCRETION_DISK_SCALE;

        public BlackHoleShader(ResourceProvider provider, ResourceLocation shaderLocation, VertexFormat format) throws IOException {
            super(provider, shaderLocation, format);
            TextureManager manager = Minecraft.getInstance().textureManager;
            setLinearWrap(manager, FIRE_NOISE);
            setLinearWrap(manager, BLOOM_CIRCLE_SMALL);
            setLinearWrap(manager, WAVY_BLOTCH_NOISE);
            setLinearWrap(manager, PSYCHEDELIC);
            setLinearWrap(manager, DENDRITIC_NOISE);
            GlStateManager._bindTexture(0);

            Map<String, Uniform> map = uniforms.stream().collect(Collectors.toMap(Uniform::getName, uniform -> uniform));

            BLACK_HOLE_RADIUS =  map.get("BlackHoleRadius");
            ACCRETION_DISK_RADIUS = map.get("AccretionDiskRadius");
            ASPECT_RATIO_CORRECTION_FACTOR = map.get("AspectRatioCorrectionFactor");
            CAMERA_ANGLE = map.get("CameraAngle");
            ZOOM = map.get("Zoom");
            CAMERA_ROTATION_AXIS = map.get("CameraRotationAxis");
            BLACK_HOLE_CENTER = map.get("BlackHoleCenter");
            ACCRETION_DISK_COLOR = map.get("AccretionDiskColor");
            ACCRETION_DISK_SCALE = map.get("AccretionDiskScale");
        }
    }

    public static class SunShader extends ShaderInstance {
        private final Uniform CORONA_INTENSITY_FACTOR;
        private final Uniform MAIN_COLOR;
        private final Uniform DARKER_COLOR;
        private final Uniform SUBTRACTIVE_ACCENT_FACTOR;
        private final Uniform SPHERE_SPIN_TIME;

        public SunShader(ResourceProvider provider) throws IOException {
            super(provider, CalamityCurios.ModResource("sun"), DefaultVertexFormat.NEW_ENTITY);
            Map<String, Uniform> map = uniforms.stream().collect(Collectors.toMap(Uniform::getName, uniform -> uniform));

            CORONA_INTENSITY_FACTOR =  map.get("CoronaIntensityFactor");
            MAIN_COLOR = map.get("MainColor");
            DARKER_COLOR = map.get("DarkerColor");
            SUBTRACTIVE_ACCENT_FACTOR = map.get("SubtractiveAccentFactor");
            SPHERE_SPIN_TIME = map.get("SphereSpinTime");
        }

        public static ShaderInstance createRadialShineShader(ResourceProvider provider) throws IOException {
            return new ShaderInstance(provider, CalamityCurios.ModResource("radial_shine"), DefaultVertexFormat.POSITION_COLOR_TEX);
        }
    }

    private static void setLinearWrap(TextureManager manager, ResourceLocation texture) {
        GlStateManager._bindTexture(manager.getTexture(texture).getId());
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
    }

    public static BlackHoleShader getBlackInstance() {
        return BLACK_HOLE_INSTANCE;
    }

    public static SunShader getSunInstance() {
        return SUN_INSTANCE;
    }

    public static void setInstance(ShaderInstance shader) {
        if (shader instanceof BlackHoleShader instance)
            CalamityCelestialBodyShader.BLACK_HOLE_INSTANCE = instance;
        else if (shader instanceof SunShader instance)
            CalamityCelestialBodyShader.SUN_INSTANCE = instance;
    }

    public static void setHoleUniform(float blackHoleRadius, Vector3f blackHoleCenter, float aspectRatioCorrectionFactor,
                                      Vector3f accretionDiskColor, float cameraAngle, Vector3f cameraRotationAxis,
                                      Vector3f accretionDiskScale, float zoomX, float zoomY, float accretionDiskRadius) {
        BLACK_HOLE_INSTANCE.BLACK_HOLE_RADIUS.set(blackHoleRadius);
        BLACK_HOLE_INSTANCE.BLACK_HOLE_CENTER.set(blackHoleCenter);
        BLACK_HOLE_INSTANCE.ASPECT_RATIO_CORRECTION_FACTOR.set(aspectRatioCorrectionFactor);
        BLACK_HOLE_INSTANCE.ACCRETION_DISK_COLOR.set(colorNormalization(accretionDiskColor));
        BLACK_HOLE_INSTANCE.CAMERA_ANGLE.set(cameraAngle);
        BLACK_HOLE_INSTANCE.CAMERA_ROTATION_AXIS.set(cameraRotationAxis);
        BLACK_HOLE_INSTANCE.ACCRETION_DISK_SCALE.set(accretionDiskScale);
        BLACK_HOLE_INSTANCE.ZOOM.set(zoomX, zoomY);
        BLACK_HOLE_INSTANCE.ACCRETION_DISK_RADIUS.set(accretionDiskRadius);
    }

    private static Vector3f colorNormalization(Vector3f color) {
        color.set(color.x() / 255f, color.y() / 255f, color.z() / 255f);
        return color;
    }

    public static void setSunUniform(float coronaIntensityFactor, Vector3f mainColor, Vector3f darkerColor,
                                     Vector3f subtractiveAccentFactor, float sphereSpinTime) {
        SUN_INSTANCE.CORONA_INTENSITY_FACTOR.set(coronaIntensityFactor);
        SUN_INSTANCE.MAIN_COLOR.set(colorNormalization(mainColor));
        SUN_INSTANCE.DARKER_COLOR.set(colorNormalization(darkerColor));
        SUN_INSTANCE.SUBTRACTIVE_ACCENT_FACTOR.set(colorNormalization(subtractiveAccentFactor));
        SUN_INSTANCE.SPHERE_SPIN_TIME.set(sphereSpinTime);
    }
}
