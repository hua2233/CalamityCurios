package hua223.calamity.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class AdrenalineHud implements IGuiOverlay {
    //Switch to texture set to reduce texture context switching
    private static TextureAtlasSprite TEXTURE;
    private static TextureAtlasSprite NANO;
    private static TextureAtlasSprite BAR;
    private static TextureAtlasSprite NANO_BAR;
    private static TextureAtlasSprite DISPLAY;
    private static TextureAtlasSprite ANIMATION;
    private static TextureAtlasSprite NANO_ANIMATION;
    private static TextureAtlasSprite NANO_FULL;

    private static boolean adrenalinEnabled;
    public static boolean isNanoMachinesMode = true;
    private static boolean isAdrenalineAnimation;
    private static boolean isNanoAnimation;
    private static boolean hasAdrenalineItem;
    private static int adrenalineItemCount;
    private static float adrenalineProgress;

    private static byte nanoTick;
    private static short lastNanoTick;
    private static byte tick;
    private static short lastTick;
    private static void renderDisplay(GuiGraphics guiGraphics, PoseStack stack, int count, int x, int y) {
        float u = DISPLAY.getU0();
        float v = DISPLAY.getV0();
        float v1 = DISPLAY.getV1();
        float frame = (DISPLAY.getU1() - u) / 3f;
        ResourceLocation location = DISPLAY.atlasLocation();
        switch (count) {
            case 1 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-33, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u + frame, v, v1);
                stack.popPose();
            }

            case 2 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-43, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u + frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-23, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u + frame, u + frame * 2f, v, v1);
                stack.popPose();
            }

            case 3 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-33, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u += frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-53, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u += frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-13, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u,  u + frame, v, v1);
                stack.popPose();
            }
        }
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!adrenalinEnabled) return;

        int x = screenWidth / 2;
        int y = screenHeight / 2;
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        PoseStack poseStack = guiGraphics.pose();

        if (isNanoMachinesMode) {
            TextureAtlasSprite sprite = isNanoAnimation ? NANO_FULL : NANO;
            float v = sprite.getV0();
            int animationFrame = isNanoAnimation ? 5 : 11;
            float frame = (sprite.getV1() - v) / animationFrame;

            int textureIndex = nanoTick / 3;
            if (textureIndex > animationFrame) {
                nanoTick = 3;
                textureIndex = 1;
            } else if (RenderUtil.getLocalTick() != lastNanoTick) {
                nanoTick++;
                lastNanoTick = RenderUtil.getLocalTick();
            }

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-105, -130f, 0);

            float tailFrame = frame * textureIndex;
            guiGraphics.innerBlit(sprite.atlasLocation(), x, x + 104, y, y + 36, 0,
                sprite.getU0(), sprite.getU1(), v + tailFrame - frame, v + tailFrame);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-92, -113f, 0);
            float startFrame = NANO_BAR.getU0();
            guiGraphics.innerBlit(NANO_BAR.atlasLocation(), x, (int) (x + 80 * adrenalineProgress), y, y + 10, 0,
                startFrame, startFrame + (NANO_BAR.getU1() - startFrame) * adrenalineProgress, NANO_BAR.getV0(), NANO_BAR.getV1());
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-105, -126f, 0);
            guiGraphics.blit(x, y, 0, 104, 32, TEXTURE);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-92, -111f, 0);

            float startFrame = BAR.getU0();
            guiGraphics.innerBlit(BAR.atlasLocation(), x, (int) (x + 80 * adrenalineProgress), y, y + 8, 0,
                startFrame, startFrame + (BAR.getU1() - startFrame) * adrenalineProgress, BAR.getV0(), BAR.getV1());
            poseStack.popPose();
        }

        if (hasAdrenalineItem) renderDisplay(guiGraphics, poseStack, adrenalineItemCount, x, y);
        if (isAdrenalineAnimation) {
            int textureIndex = tick / 2;

            if (textureIndex > 10) {
                isAdrenalineAnimation = false;
                tick = 2;
                return;
            } else if (RenderUtil.getLocalTick() != lastTick) {
                tick++;
                lastTick = RenderUtil.getLocalTick();
            }

            TextureAtlasSprite sprite = isNanoMachinesMode ? NANO_ANIMATION : ANIMATION;
            float v = sprite.getV0();
            float frame = (sprite.getV1() - v) / 10;
            float tailFrame = frame * textureIndex;

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-144, isNanoMachinesMode ? - 142 : -146, 0);
            guiGraphics.innerBlit(sprite.atlasLocation(), x, x + 172, y,
                y + 70, 0, sprite.getU0(), sprite.getU1(), v + tailFrame - frame, v + tailFrame);
            poseStack.popPose();
        }
    }

    public static void setForMachinesMode(boolean isNano) {
        isNanoMachinesMode = isNano;
    }

    public static void playAnimation(boolean play) {
        isAdrenalineAnimation = play;
        if (isNanoMachinesMode) setNanoAnimation(play);
    }

    public static void setNanoAnimation(boolean offOrOn) {
        AdrenalineHud.lastNanoTick = 0;
        AdrenalineHud.nanoTick = 3;
        isNanoAnimation = offOrOn;
    }

    public static void setAdrenalineCount(int count) {
        adrenalineItemCount = count;
        if (count > 0) hasAdrenalineItem = true;
    }

    public static void setAdrenalineProgress(int value) {
        adrenalineProgress = value / 30f;

    }

    public static void setAdrenalineEnabled(boolean offOrOn) {
        adrenalinEnabled = offOrOn;
        if (!adrenalinEnabled) {
            adrenalineProgress = 0;
            hasAdrenalineItem = false;
            isNanoMachinesMode = false;
            adrenalineItemCount = 0;
        }
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        TEXTURE = atlas.getSprite(CalamityCurios.ModResource("adrenaline_hud"));
        NANO = atlas.getSprite(CalamityCurios.ModResource("nano_machines"));
        BAR = atlas.getSprite(CalamityCurios.ModResource("adrenaline_bar"));
        NANO_BAR = atlas.getSprite(CalamityCurios.ModResource("nano_bar"));
        DISPLAY = atlas.getSprite(CalamityCurios.ModResource("adrenaline_display"));
        ANIMATION = atlas.getSprite(CalamityCurios.ModResource("adrenaline_animation"));
        NANO_ANIMATION = atlas.getSprite(CalamityCurios.ModResource("nano_animation"));
        NANO_FULL = atlas.getSprite(CalamityCurios.ModResource("nano_full"));
    }
}
