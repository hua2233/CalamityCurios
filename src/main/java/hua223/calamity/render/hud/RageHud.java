package hua223.calamity.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
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
public class RageHud implements IGuiOverlay {
    private static TextureAtlasSprite RAGE;
    private static TextureAtlasSprite BAR;
    private static TextureAtlasSprite DISPLAY;
    private static TextureAtlasSprite ANIMATIONS;

    public static boolean rageEnabled;
    private static boolean animationFrameTime;
    public static byte shatteredLevel;
    public static float rageProgress;
    public static boolean hasRageItem;
    public static int rageItemCount;
    public static int levelUpProgress;
    private static int currentDamage;
    private static double levelUpDamage = 100;
    private static byte tick;
    private static short lastTick;

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!rageEnabled) return;
        int x = screenWidth / 2;
        int y = screenHeight / 2;
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        PoseStack stack = guiGraphics.pose();

        stack.pushPose();
        stack.scale(0.4f, 0.4f, 1f);
        stack.translate(0, -130f, 0);
        guiGraphics.blit(x, y, 0,104, 36, RAGE);
        stack.popPose();

        stack.pushPose();
        stack.scale(0.4f, 0.4f, 1f);
        stack.translate(13, -111f, 0);
        float startFrame = BAR.getU0();
        guiGraphics.innerBlit(BAR.atlasLocation(), x, (int) (x + 80 * rageProgress), y, y + 8, 0,
            startFrame, startFrame + (BAR.getU1() - startFrame) * rageProgress, BAR.getV0(), BAR.getV1());
        stack.popPose();

        if (hasRageItem) renderDisplay(guiGraphics, stack, rageItemCount, x, y);

        if (animationFrameTime) {
            var textureIndex = tick / 2;

            if (textureIndex > 10) {
                animationFrameTime = false;
                tick = 2;
                return;
            } else if (RenderUtil.getLocalTick() != lastTick) {
                tick++;
                lastTick = RenderUtil.getLocalTick();
            }

            float v = ANIMATIONS.getV0();
            float frame = (ANIMATIONS.getV1() - v) / 10;
            float tailFrame = frame * textureIndex;
            stack.pushPose();
            stack.scale(0.4f, 0.4f, 1f);
            stack.translate(-24, -130f, 0);
            guiGraphics.innerBlit(ANIMATIONS.atlasLocation(), x, x + 152, y,
                y + 38, 0, ANIMATIONS.getU0(), ANIMATIONS.getU1(), v + tailFrame - frame, v + tailFrame);
            stack.popPose();
        }
    }

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
                stack.translate(88, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u + frame, v, v1);
                stack.popPose();
            }

            case 2 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(78, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u + frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(98, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u + frame, u + frame * 2f, v, v1);
                stack.popPose();
            }

            case 3 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(88, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u += frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(69, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u, u += frame, v, v1);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(108, -98f, 0);
                guiGraphics.innerBlit(location, x, x + 12, y, y + 12, 0, u,  u + frame, v, v1);
                stack.popPose();
            }
        }
    }

    public static void setRageProgress(float rageValue) {
        rageProgress = rageValue / 100f;
    }

    public static void setRageCount(int count) {
        rageItemCount = count;
        if (count > 0) hasRageItem = true;
    }

    public static void setCurrentDamage(int damage) {
        currentDamage = damage;
        levelUpProgress = (int) ((currentDamage / levelUpDamage) * 100);
    }

    public static void setShatteredLevel(byte shatteredLevel, int upDamage) {
        RageHud.shatteredLevel = shatteredLevel;
        RageHud.currentDamage = 0;
        RageHud.levelUpDamage = upDamage;
        RageHud.levelUpProgress = 0;
    }

    @SuppressWarnings("ConstantConditions")
    public static void playAnimation() {
        animationFrameTime = true;
        CalamitySounds.FULL_RAGE.playLocalSound();
    }

    public static int getLevelBonus() {
        return 35 + shatteredLevel * 2;
    }

    public static void setLevel(byte level) {
        shatteredLevel = level;
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        RAGE = atlas.getSprite(CalamityCurios.ModResource("rage_hud"));
        DISPLAY = atlas.getSprite(CalamityCurios.ModResource("rage_display"));
        ANIMATIONS = atlas.getSprite(CalamityCurios.ModResource("rage_animation"));
        BAR = atlas.getSprite(CalamityCurios.ModResource("rage_bar"));
    }
}
