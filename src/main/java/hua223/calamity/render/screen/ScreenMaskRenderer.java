package hua223.calamity.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public class ScreenMaskRenderer extends ScreenEffectRenderer {
    private final ResourceLocation mask;
    private boolean stop;
    private final float[] colorMixer = new float[] {1f, 1f, 1f, 1f};

    public ScreenMaskRenderer(ResourceLocation mask) {
        this.mask = mask;
    }

    public void setColorMixer(float r, float g, float b, float a) {
        colorMixer[0] = r;
        colorMixer[1] = g;
        colorMixer[2] = b;
        colorMixer[3] = a;
    }

    public void setSingleChannel(int rgbaChannel, float value) {
        if (rgbaChannel < 4) colorMixer[rgbaChannel] =  value;
    }

    public void stop() {
        stop = true;
    }

    @Override
    public boolean render(float partialTick, Minecraft minecraft) {
        RenderSystem.setShaderTexture(0, mask);
        RenderSystem.setShaderColor(colorMixer[0], colorMixer[1], colorMixer[2], colorMixer[3]);
        renderMask(minecraft);
        return stop;
    }

    @Override
    public int getPriority() {
        return 150;
    }
}
