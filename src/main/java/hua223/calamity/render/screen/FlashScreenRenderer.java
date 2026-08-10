package hua223.calamity.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlashScreenRenderer extends ScreenEffectRenderer {
    private final short flashStartTime;
    private final float baseFlashIntensity;
    private final float[] flashColor;
    private final int fadeInTime;
    private final int flashTime;
    private final ResourceLocation texture;

    @SuppressWarnings("ConstantConditions")
    public FlashScreenRenderer(int time, float intensity, int c) {
        this.flashStartTime = RenderUtil.getLocalTick();
        flashTime = time;
        fadeInTime = (int) (time * 0.5f);
        baseFlashIntensity = intensity;
        flashColor = new float[] {
            FastColor.ARGB32.red(c) / 255f,
            FastColor.ARGB32.green(c) / 255f,
            FastColor.ARGB32.blue(c) / 255f};
        texture = CalamityCurios.ModResource("textures/misc/flash.png");
    }

    public FlashScreenRenderer(int time, float intensity) {
        this(time, intensity, 0xFFFFFFFF);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean render(float partialTick, Minecraft minecraft) {
        int tick = RenderUtil.processingCycleTime(flashStartTime);
        if (tick == flashTime) return true;

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(flashColor[0], flashColor[1],
            flashColor[2], getIntensity(partialTick, tick, minecraft));
        renderMask(minecraft);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        return false;
    }

    @Override
    public int getPriority() {
        return 500;
    }

    private float getIntensity(float partialTick, int age, Minecraft minecraft) {
        float adjustedAge = age + partialTick;
        float screen = minecraft.options.screenEffectScale().get().floatValue();

        if (age <= fadeInTime) {
            return baseFlashIntensity * (adjustedAge / fadeInTime) * screen;
        } else {
            float fadeOutDuration = (float) (flashTime - fadeInTime);
            float fadeOutProgress = (adjustedAge - fadeInTime) / fadeOutDuration;
            return baseFlashIntensity * (1.0f - fadeOutProgress) * screen;
        }
    }
}
