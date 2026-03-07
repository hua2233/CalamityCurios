package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.clientInfos.Adrenaline;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class AdrenalineHud implements IGuiOverlay {
    private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/hud/adrenaline_hud.png");
    private static final ResourceLocation NANO = CalamityCurios.ModResource("textures/hud/nano_machines.png");
    private static final ResourceLocation BAR = CalamityCurios.ModResource("textures/hud/adrenaline_bar.png");
    private static final ResourceLocation NANO_BAR = CalamityCurios.ModResource("textures/hud/nano_bar.png");
    private static final ResourceLocation DISPLAY = CalamityCurios.ModResource("textures/hud/adrenaline_display.png");
    private static final ResourceLocation ANIMATION = CalamityCurios.ModResource("textures/hud/adrenaline_animation.png");
    private static final ResourceLocation NANO_ANIMATION = CalamityCurios.ModResource("textures/hud/nano_animation.png");
    private static final ResourceLocation NANO_FULL = CalamityCurios.ModResource("textures/hud/nano_full.png");
    public static byte nanoTick;
    public static short lastNanoTick;
    private static byte tick;
    private static short lastTick;

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!Adrenaline.adrenalinEnabled) return;
        int x = screenWidth / 2;
        int y = screenHeight / 2;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (Adrenaline.isNanoMachinesMode) {
            ResourceLocation t;
            int height;
            int count;
            if (Adrenaline.isNanoAnimation) {
                t = NANO_FULL;
                height = 180;
                count = 5;
            } else {
                t = NANO;
                height = 396;
                count = 11;
            }

            RenderSystem.setShaderTexture(0, t);

            short currentTick = RenderUtil.getLocalTick();
            if (currentTick != lastNanoTick) {
                nanoTick++;
                if (!Adrenaline.isAdrenalineAnimation) lastNanoTick = currentTick;
            }

            int textureIndex = (nanoTick / 3) % count;
            if (textureIndex == count - 1) nanoTick = 0;

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-105, -130f, 0);
            GuiComponent.blit(poseStack, x, y, 0, (textureIndex * 36), 104, 36, 104, height);
            poseStack.popPose();

            RenderSystem.setShaderTexture(0, NANO_BAR);
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-92, -113f, 0);
            GuiComponent.blit(poseStack, x, y, 0, 0, Adrenaline.getAdrenalineProgress(), 10, 80, 10);
            poseStack.popPose();
        } else {
            RenderSystem.setShaderTexture(0, TEXTURE);
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-105, -126f, 0);
            GuiComponent.blit(poseStack, x, y, 0, 0, 104, 32, 104, 32);
            poseStack.popPose();


            RenderSystem.setShaderTexture(0, BAR);
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-92, -111f, 0);
            GuiComponent.blit(poseStack, x, y, 0, 0, Adrenaline.getAdrenalineProgress(), 8, 80, 8);
            poseStack.popPose();
        }

        if (Adrenaline.isAdrenalineAnimation) {
            short currentTick = RenderUtil.getLocalTick();

            if (currentTick != lastTick) {
                tick++;
                lastTick = currentTick;
            }

            int textureIndex = (tick / 2) % 10;
            if (textureIndex == 9) {
                Adrenaline.isAdrenalineAnimation = false;
                tick = 0;
            }

            ResourceLocation t;
            int y1;
            if (Adrenaline.isNanoMachinesMode) {
                t = NANO_ANIMATION;
                y1 = -142;
            } else {
                y1 = -146;
                t = ANIMATION;
            }

            RenderSystem.setShaderTexture(0, t);

            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 1f);
            poseStack.translate(-144, y1, 0);
            GuiComponent.blit(poseStack, x, y, 0, (textureIndex * 70), 172, 70, 172, 700);
            poseStack.popPose();
        }


        if (Adrenaline.hasAdrenalineItem) renderDisplay(poseStack, Adrenaline.adrenalineItemCount, x, y);
    }

    private static void renderDisplay(PoseStack stack, int count, int x, int y) {
        RenderSystem.setShaderTexture(0, DISPLAY);
        switch (count) {
            case 1 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-33, -98f, 0);
                GuiComponent.blit(stack, x, y, 12, 0, 12, 12, 36, 12);
                stack.popPose();
            }

            case 2 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-43, -98f, 0);
                GuiComponent.blit(stack, x, y, 12, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-23, -98f, 0);
                GuiComponent.blit(stack, x, y, 0, 0, 12, 12, 36, 12);
                stack.popPose();
            }

            case 3 -> {
                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-33, -98f, 0);
                GuiComponent.blit(stack, x, y, 12, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-53, -98f, 0);
                GuiComponent.blit(stack, x, y, 0, 0, 12, 12, 36, 12);
                stack.popPose();

                stack.pushPose();
                stack.scale(0.35f, 0.35f, 1f);
                stack.translate(-13, -98f, 0);
                GuiComponent.blit(stack, x, y, 24, 0, 12, 12, 36, 12);
                stack.popPose();
            }
        }
    }
}
