package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
//Standardization, the previous version was a bit too messy and disorganized...
public abstract class EnergyFrameType implements IGuiOverlay {
    //Switch to texture set to reduce texture context switching
    protected TextureAtlasSprite BAR;
    protected TextureAtlasSprite MAIN;
    protected TextureAtlasSprite DISPLAY;
    protected TextureAtlasSprite ANIMATIONS;

    protected short tick;
    protected short barTick;
    protected boolean active;
    protected boolean enabled;
    protected byte displayCount;
    protected short animationTick = -1;

    protected float progress;
    protected float lastProgress;
    protected float finalProgress;

    protected final float[] barTextureInfo;
    protected final float[] mainTextureInfo;
    protected final float[] animationTextureInfo;

    protected static final RandomSource SOURCE = Minecraft.getInstance().font.random;

    protected EnergyFrameType(int mainTextHeight, float renderX, float renderY, float scale) {
        mainTextureInfo = createMainTextureInfo(mainTextHeight, renderX, renderY, scale);
        barTextureInfo = createBarTextureInfo(mainTextureInfo, getBarWHAndBorderWH());
        animationTextureInfo = createAnimationTextureInfo(mainTextureInfo, getBaseAnimation());
    }

    protected final float[] createMainTextureInfo(int mainTextHeight, float renderX, float renderY, float scale) {
        return new float[] {104, mainTextHeight, renderX, renderY, renderX + 104 * scale, renderY + mainTextHeight * scale, scale};
    }

    protected final float[] createBarTextureInfo(float[] mainTextureInfo, Vector4i info) {
        float renderX = mainTextureInfo[2] + info.z * mainTextureInfo[6];
        float barWidth = info.x * mainTextureInfo[6];
        float renderY = mainTextureInfo[3] + info.w * mainTextureInfo[6];
        float barHeight = info.y * mainTextureInfo[6];
        return new float[] {renderX, barWidth, renderY, barHeight};
    }

    protected final float[] createAnimationTextureInfo(float[] mainTextureInfo, Vector4i animation) {
        int animationWidth = animation.x;
        float offsetX = mainTextureInfo[2] + (mainTextureInfo[0] - animationWidth) / 2f * mainTextureInfo[6];
        int animationHeight = animation.y / animation.z;
        float offsetY = mainTextureInfo[3] + (mainTextureInfo[1] - animationHeight) / 2f * mainTextureInfo[6];

        return new float[] {offsetX, offsetY, offsetX + animationWidth * mainTextureInfo[6], offsetY + animationHeight * mainTextureInfo[6], animation.z, animation.w};
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (enabled) {
            if (tick != RenderUtil.getLocalTick()) {
                tick = RenderUtil.getLocalTick();
                onTick();
            }

            if (barTick <= 10) progress = barTick == 10 ? finalProgress :
                Mth.lerp((barTick + partialTick) / 10f, lastProgress, finalProgress);

            if (progress == 0) active = false;
            internalRender(guiGraphics);
        }
    }

    protected void onTick() {
        if (barTick < 10) ++barTick;
        if (animationTick > -1) --animationTick;
    }

    protected final void renderAnimation(int tick, TextureAtlasSprite sprite, PoseStack stack, float[] animationTextureInfo) {
        if (tick > -1) {
           int frameIndex = (int) (tick > animationTextureInfo[5] ? animationTextureInfo[4] -
               Math.ceil(tick / animationTextureInfo[5]) : animationTextureInfo[4] - 1);

           float v0 = sprite.getV0();
           float frame = (sprite.getV1() - v0) / animationTextureInfo[4];
           v0 += frame * frameIndex;

           renderSprite(sprite.atlasLocation(), stack, animationTextureInfo[0], animationTextureInfo[1],
               animationTextureInfo[2], animationTextureInfo[3], sprite.getU0(), sprite.getU1(), v0, v0 + frame);
        }
    }

    protected void internalRender(GuiGraphics guiGraphics) {
        PoseStack stack = guiGraphics.pose();
        if (active && (tick & 1) == 0) setShakeOffset(stack);
        renderMain(mainTextureInfo, stack);
        renderBar(barTextureInfo, stack);
        renderDisplay(mainTextureInfo, stack);
        renderAnimation(animationTick, ANIMATIONS, stack, animationTextureInfo);
        if (active && (tick & 1) == 0) stack.popPose();
    }

    //Bar Width | Bar Height | YOffset | Border Width
    protected abstract Vector4i getBarWHAndBorderWH();

    //Width | Height | Frame Count | Frame Tick
    protected abstract Vector4i getBaseAnimation();

    protected void renderMain(float[] textureInfo, PoseStack stack) {
        renderSprite(MAIN, stack, textureInfo[2], textureInfo[3], textureInfo[4], textureInfo[5]);
    }

    public void setDisplayCount(int count) {
        displayCount = (byte) count;
    }

    public abstract void active();

    protected void renderDisplay(float[] textureInfo, PoseStack stack) {
        if (displayCount > 0) {
            float u = DISPLAY.getU0();
            float v = DISPLAY.getV0();
            float v1 = DISPLAY.getV1();
            float frame = (DISPLAY.getU1() - u) / 3f;

            int[] frameIndex = new int[] {0, -1, 1};
            float widgetWH = 12 * textureInfo[6];
            float widgetInterval = 4 * textureInfo[6];
            ResourceLocation location = DISPLAY.atlasLocation();
            float yPos = getDisplayYOffset() * textureInfo[6];
            float mainCenter = textureInfo[2] + textureInfo[0] * textureInfo[6] / 2f + getDisplayXCenterOffset();

            for (int i = 0; i < displayCount; i++) {
                float u0 = u + (frame * i);
                float xPos = mainCenter + (widgetWH + widgetInterval) * frameIndex[i];
                renderSprite(location, stack, xPos, yPos, xPos + widgetWH, yPos + widgetWH, u0, u0 + frame, v, v1);
            }
        }
    }

    protected void renderBar(float[] barTextureInfo, PoseStack stack) {
        if (progress > 0) {
            float u0 = BAR.getU0();
            float u = BAR.getU1() - u0;
            float renderX = barTextureInfo[0];

            renderSprite(BAR.atlasLocation(), stack, renderX, barTextureInfo[2], renderX + barTextureInfo[1] *
                    progress, barTextureInfo[2] + barTextureInfo[3], u0,  u0 + u * progress, BAR.getV0(), BAR.getV1());
        }
    }

    public void setProgress(float value) {
        //During smooth computation, the server does not need to wait.
        //if it were to be sent out again，immediately stop the smooth animation that should have been stopped this time
        if (active && finalProgress == 0) active = false;

        barTick = 0;
        lastProgress = progress;
        finalProgress = value / getMaxValue();
    }

    protected void clear() {
        tick = 0;
        barTick = 0;
        progress = 0;
        active = false;
        lastProgress = 0;
        finalProgress = 0;
        animationTick = -1;
    }

    protected abstract float getMaxValue();

    protected float getDisplayXCenterOffset() {
        return -2.2f;
    }

    protected int getDisplayYOffset() {
        return 31;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SuppressWarnings("ConstantConditions")
    public void playAnimation() {
        animationTick = (short) (animationTextureInfo[4] * animationTextureInfo[5]);
    }

    protected static void setShakeOffset(PoseStack stack) {
        float shake = 1f;

        float shakeX = Mth.nextFloat(SOURCE, -shake, shake);
        float shakeY = Mth.nextFloat(SOURCE, -shake, shake);

        stack.pushPose();
        stack.translate(shakeX, shakeY, 0);
    }

    protected static void renderSprite(TextureAtlasSprite sprite, PoseStack stack, float x, float y, float x1, float y1) {
        renderSprite(sprite.atlasLocation(), stack, x, y, x1, y1, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    protected static void renderSprite(ResourceLocation location, PoseStack stack, float x, float y, float x1, float y1, float u0, float u1, float v0, float v1) {
        Matrix4f matrix4f = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShaderTexture(0, location);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x, y, 0).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, x, y1, 0).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y1, 0).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y, 0).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
