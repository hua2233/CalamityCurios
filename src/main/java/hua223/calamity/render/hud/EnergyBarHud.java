package hua223.calamity.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public abstract class EnergyBarHud implements IGuiOverlay {
    protected int color;
    protected float progress;
    protected final int yOffset;
    protected byte tickProgress;
    protected float lastProgress;
    protected float finalProgress;
    protected float frameProgress;
    public boolean notRender = true;

    protected TextureAtlasSprite MAIN_TEXTURE;
    private static TextureAtlasSprite FRAME_TEXTURE;
    protected static short tick;

    protected EnergyBarHud(int yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (notRender) return;

        if (tick != RenderUtil.getLocalTick()) {
            tick = RenderUtil.getLocalTick();
            onTick();
        }

        int x = screenWidth / 2;
        int y = screenHeight / 2 - yOffset;
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        PoseStack stack = guiGraphics.pose();

        stack.pushPose();
        stack.scale(0.6f, 0.5f, 1f);
        stack.translate(-200, 350f, 0);
        guiGraphics.blit(x, y, 0, 175, 20, FRAME_TEXTURE);
        stack.popPose();

        stack.pushPose();
        stack.scale(0.6f, 0.5f, 1f);
        stack.translate(-178, 352f, 0);
        float partialFrame = obtainPartialProgress(partialTick);

        guiGraphics.fill(x, y, (int) (x + 150 * partialFrame), y + 16, color);
        stack.popPose();

        stack.pushPose();
        stack.scale(0.2f, 0.2f, 1f);
        stack.translate(-118, 1075f, 0);
        renderMain(guiGraphics, x, y);
        stack.popPose();
    }

    public void start() {
        notRender = false;
        progress = 0;
        tickProgress = 0;
        frameProgress = 0;
    }

    public abstract float getMaxValue();

    public void close() {
        notRender = true;
    }

    protected abstract void renderMain(GuiGraphics guiGraphics, int x, int y);

    //Interpolate between Ticks to balance the growth of energy
    public void setProgress(float value) {
        lastProgress = progress;
        finalProgress = Mth.clamp(value / getMaxValue(), 0f, 1f);
        tickProgress = 0;
    }

    protected void onTick() {
        if (progress == finalProgress) return;

        frameProgress = progress;
        if (++tickProgress >= 20) {
            progress = finalProgress;
            tickProgress = 0;
        } else progress = Mth.lerp(tickProgress / 20f, lastProgress, finalProgress);

        onTickProgress();
    }

    protected void onTickProgress() {}

    //Interpolate between frames to obtain smooth animation
    private float obtainPartialProgress(float partialTick) {
        return frameProgress == progress ? progress :
            (frameProgress = Mth.lerp(partialTick, frameProgress, progress));
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        FRAME_TEXTURE = atlas.getSprite(CalamityCurios.ModResource("energy_frame"));
    }
}
