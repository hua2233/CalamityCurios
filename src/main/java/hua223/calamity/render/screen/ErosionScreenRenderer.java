package hua223.calamity.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.render.shaders.ErosionShader;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ErosionScreenRenderer extends ScreenEffectRenderer {
    private final short startTime;
    private final int effectTime;

    @SuppressWarnings("ConstantConditions")
    public ErosionScreenRenderer(int effectTime) {
        startTime = RenderUtil.getLocalTick();
        this.effectTime = effectTime;
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

            brightnessInterpolant = RenderUtil.clampLerp(0.1f, 1f, brightnessInterpolant);
            int width = minecraft.getWindow().getGuiScaledWidth();
            float height = minecraft.getWindow().getGuiScaledHeight();

            ErosionShader.prepareShader(0.05f, Mth.lerp(brightnessInterpolant, 6f, 3.97f),
                Mth.lerp(brightnessInterpolant, 3f, 20f), (float) (Math.sqrt(brightnessInterpolant) * 0.95f),
                width / height, RenderUtil.clampLerp(effectTime * 0.085f, effectTime, partialTick) * 1.2f);

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
