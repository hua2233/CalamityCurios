package hua223.calamity.render.shaders;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.function.Supplier;

public class ErosionShader extends CalamityShaders.CalamityShaderInstance {
    private static Supplier<ShaderInstance> EROSION_SHADER;
    private final Uniform ANIMATION_SPEED;
    private final Uniform VIGNETTE_POWER;
    private final Uniform VIGNETTE_BRIGHTNESS;
    private final Uniform CRACK_BRIGHTNESS;
    private final Uniform ASPECT_RATIO_CORRECTION_FACTOR;
    private final Uniform RADIAL_OFFSET_TIME;

    protected ErosionShader(ResourceProvider provider) throws IOException {
        super(provider, CalamityCurios.ModResource("fleshy_vignette"), DefaultVertexFormat.POSITION_TEX);
        ANIMATION_SPEED = getUniform("AnimationSpeed");
        VIGNETTE_POWER = getUniform("VignettePower");
        VIGNETTE_BRIGHTNESS = getUniform("VignetteBrightness");
        CRACK_BRIGHTNESS = getUniform("CrackBrightness");
        ASPECT_RATIO_CORRECTION_FACTOR = getUniform("AspectRatioCorrectionFactor");
        RADIAL_OFFSET_TIME = getUniform("RadialOffsetTime");
        EROSION_SHADER = () -> this;
    }

    public static void prepareShader(float animationSpeed, float vignettePower, float vignetteBrightness,
                                     float crackBrightness, float aspectRatioCorrectionFactor, float radialOffsetTime) {
        ErosionShader instance = (ErosionShader) EROSION_SHADER.get();
        instance.ANIMATION_SPEED.set(animationSpeed);
        instance.VIGNETTE_POWER.set(vignettePower);
        instance.VIGNETTE_BRIGHTNESS.set(vignetteBrightness);
        instance.CRACK_BRIGHTNESS.set(crackBrightness);
        instance.ASPECT_RATIO_CORRECTION_FACTOR.set(aspectRatioCorrectionFactor);
        instance.RADIAL_OFFSET_TIME.set(radialOffsetTime);

        RenderSystem.setShader(EROSION_SHADER);
        RenderSystem.setShaderTexture(0, CalamityShaders.CRACKED_NOISE);
    }
}
