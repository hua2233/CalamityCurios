package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class FatigueHud implements IGuiOverlay {
    private static final ResourceLocation INNER_RING = CalamityCurios.ModResource("textures/hud/fatigue1.png");
    private static final ResourceLocation OUTER_RING = CalamityCurios.ModResource("textures/hud/fatigue2.png");
    private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/hud/fatigue3.png");
    private static final ResourceLocation CHASSIS = CalamityCurios.ModResource("textures/hud/fatigue4.png");
    private static final int INNER_RING_START_COLOR = 0xCC1900;
    private static final int INNER_RING_END_COLOR = 0x008000;
    private static final int OUTER_RING_START_COLOR = 0x3D0700;
    private static final int OUTER_RING_END_COLOR = 0x002600;
    private static final int CHASSIS_START_COLOR = 0x500A00;
    private static final int CHASSIS_END_COLOR = 0x143800;
    public static boolean notRender = true;
    private static float speed;
    private static float percentage;
    private static float finalPercentage;
    private static short lastTick = 0;

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (notRender) return;

        int x = screenWidth + 150;
        int y = screenHeight + 130;
        short t = RenderUtil.getLocalTick();
        if (t != lastTick) {
            lastTick = t;
            updatePercentage();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        poseStack.pushPose();
        poseStack.scale(0.4f, 0.4f, 1f);
        poseStack.translate(-550, 150, 0);

        RenderUtil.setShaderInterpolateColor(CHASSIS_START_COLOR, CHASSIS_END_COLOR, percentage);
        RenderSystem.setShaderTexture(0, CHASSIS);
        GuiComponent.blit(poseStack, x, y, 0, 0, 44, 44, 44, 44);

        RenderUtil.setShaderInterpolateColor(INNER_RING_START_COLOR, INNER_RING_END_COLOR, percentage);
        renderPercentageCircle(percentage, x, y, poseStack, INNER_RING);

        RenderUtil.setShaderInterpolateColor(OUTER_RING_START_COLOR, OUTER_RING_END_COLOR, percentage);
        renderPercentageCircle(percentage, x, y, poseStack, OUTER_RING);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, x, y, 0, 0, 44, 44, 44, 44);
        poseStack.popPose();
    }

    private static void renderPercentageCircle(float percentage, int x, int y, PoseStack stack, ResourceLocation texture) {
        if (percentage >= 1f) {
            RenderSystem.setShaderTexture(0, texture);
            GuiComponent.blit(stack, x, y, 0, 0, 44, 44, 44, 44);
        } else if (percentage > 0.03f) {
            int textureSize = 44;
            int xSize = textureSize / 2;

            RenderSystem.setShaderTexture(0, texture);
            if (percentage > 0.5f) {
                float p = percentage - 0.5f;
                int rightYSize = (int) ((p / 0.5f) * textureSize);
                if (rightYSize < 1) rightYSize = 1;

                int z = textureSize - rightYSize;
                GuiComponent.blit(stack, x, y, 0, 0, xSize, textureSize, textureSize, textureSize);
                GuiComponent.blit(stack, x + xSize, y + z, xSize, z, xSize, rightYSize, textureSize, textureSize);
            } else {
                GuiComponent.blit(stack, x, y, 0, 0, xSize, (int) ((percentage / 0.5f) * textureSize), textureSize, textureSize);
            }
        }
    }

    private static void updatePercentage() {
        if (speed != 0f) {
            percentage += speed;

            if (percentage >= finalPercentage) {
                if (percentage > 1f) percentage = 1f;
                speed = 0;
            }
        }
    }

    public static void syncPercentage(float value) {
        float p = value / 100f;
        if (p > percentage) {
            finalPercentage = p;
            speed = (p - percentage) / 20;
        } else percentage = p;
    }
}
