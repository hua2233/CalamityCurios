package hua223.calamity.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ErosionScreenRenderer extends ScreenEffectRenderer {
    private static Supplier<ShaderInstance> erosionShader;
    private final ResourceLocation noise = CalamityCurios.ModResource("textures/effect/cracked_noise.png");
    private final short startTime;
    private final int effectTime;

    @SuppressWarnings("ConstantConditions")
    public ErosionScreenRenderer(int effectTime) {
        startTime = RenderUtil.getLocalTick();
        this.effectTime = effectTime;
    }

    public static void setErosionShader(ShaderInstance shader) {
        if (erosionShader == null) erosionShader = () -> shader;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean render(float partialTick, Minecraft minecraft) {
        float tick = RenderUtil.processingCycleTime(startTime);
        if (tick == effectTime) return true;
        partialTick += tick;

        float brightnessFadeIn = RenderUtil.clampLerp( 2, effectTime * 0.1f, partialTick);
        float brightnessFadeOut = RenderUtil.clampLerp(effectTime - 1f, effectTime - 6f, partialTick);
        float brightnessInterpolant = brightnessFadeIn * brightnessFadeOut;

        if (brightnessInterpolant > 0.1f) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            ShaderInstance instance = erosionShader.get();
            brightnessInterpolant = RenderUtil.clampLerp(0.1f, 1f, brightnessInterpolant);
            int width = minecraft.getWindow().getGuiScaledWidth();
            float height = minecraft.getWindow().getGuiScaledHeight();
            instance.safeGetUniform("AnimationSpeed").set(0.05f);
            instance.safeGetUniform("VignettePower").set(Mth.lerp(brightnessInterpolant, 6f, 3.97f));
            instance.safeGetUniform("VignetteBrightness").set(Mth.lerp(brightnessInterpolant, 3f, 20f));
            instance.safeGetUniform("CrackBrightness").set((float) (Math.sqrt(brightnessInterpolant) * 0.95f));
            instance.safeGetUniform("AspectRatioCorrectionFactor").set(width / height);
            instance.safeGetUniform("RadialOffsetTime").set(RenderUtil.clampLerp(effectTime * 0.085f, effectTime, partialTick) * 1.2f);

            RenderSystem.setShader(erosionShader);
            RenderSystem.setShaderTexture(0, noise);
            renderOnlyTexture(width, height);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }

        return false;
    }

    @Override
    public int getPriority() {
        return 80;
    }
}
