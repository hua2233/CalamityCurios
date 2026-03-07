package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.clientInfos.Rage;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class RageHud implements IGuiOverlay {
    private static final ResourceLocation RAGE = CalamityCurios.ModResource("textures/hud/rage_hud.png");
    private static final ResourceLocation DISPLAY = CalamityCurios.ModResource("textures/hud/rage_display.png");
    private static final ResourceLocation ANIMATIONS = CalamityCurios.ModResource("textures/hud/rage_full_animation.png");
    private static final int TEXTURE_WIDTH = 104;
    private static final int TEXTURE_HEIGHT = 36;
    private static final int PROGRESS_HEIGHT = 8;
    private static byte tick;
    private static short lastTick;

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!Rage.rageEnabled) return;
        int x = screenWidth / 2;
        int y = screenHeight / 2;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.setShaderTexture(0, RAGE);
        poseStack.pushPose();
        poseStack.scale(0.4f, 0.4f, 1f);
        poseStack.translate(0, -130f, 0);
        GuiComponent.blit(poseStack, x, y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, 184, 36);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.scale(0.4f, 0.4f, 1f);
        poseStack.translate(13, -111f, 0);
        GuiComponent.blit(poseStack, x, y, 105, 0, Rage.rageProgress, PROGRESS_HEIGHT, 184, 36);
        poseStack.popPose();

        if (Rage.hasRageItem) renderDisplay(poseStack, Rage.rageItemCount, x, y);

        if (Rage.animationFrameTime) {
            RenderSystem.setShaderTexture(0, ANIMATIONS);
            int frameCount = 10;
            int frameTime = 2;

            short currentTick = RenderUtil.getLocalTick();
            if (currentTick != lastTick) {
                tick++;
                lastTick = currentTick;
            }

            int textureIndex = (tick / frameTime) % frameCount;

            if (textureIndex == 9) {
                Rage.animationFrameTime = false;
                tick = 0;
            }
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-24, -130f, 0);
            GuiComponent.blit(poseStack, x, y, 0, (textureIndex * 38), 152, 38, 152, 380);
            poseStack.popPose();
        }
    }

//    public static final IGuiOverlay RAGE_HUD = ((((gui, poseStack, partialTick, screenWidth, screenHeight) -> {
//
//    })));

    private static void renderDisplay(PoseStack stack, int count, int x, int y) {
        RenderSystem.setShaderTexture(0, DISPLAY);
        switch (count) {
            case 1 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(88, -98f, 0);
                GuiComponent.blit(stack, x, y, 0, 0, 12, 12, 36, 12);
                stack.popPose();
            }

            case 2 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(78, -98f, 0);
                GuiComponent.blit(stack, x, y, 0, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(98, -98f, 0);
                GuiComponent.blit(stack, x, y, 12, 0, 12, 12, 36, 12);
                stack.popPose();
            }

            case 3 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(88, -98f, 0);
                GuiComponent.blit(stack, x, y, 24, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(69, -98f, 0);
                GuiComponent.blit(stack, x, y, 0, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(108, -98f, 0);
                GuiComponent.blit(stack, x, y, 12, 0, 12, 12, 36, 12);
                stack.popPose();
            }
        }
    }
}
