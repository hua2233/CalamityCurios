package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class FatigueSlot implements IGuiOverlay {
    private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/hud/fatigue_border.png");
    private static final ResourceLocation BAR = CalamityCurios.ModResource("textures/hud/fatigue_bar.png");
    public static boolean notRender = true;
    private static int progress = 34;
    public static void setProgress(int value) {
        progress = (int) ((value / 100f) * 34);
    }

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (notRender) return;

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.pushPose();
        poseStack.scale(0.4f, 0.4f, 1f);
        int x = screenWidth + 350;
        int y = screenHeight + 330;
        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, x, y, 0, 0, 34, 70, 34, 70);

        RenderSystem.setShaderTexture(0, BAR);
        GuiComponent.blit(poseStack, x + 14, y + 18, 0, 0, 6, progress, 6, 34);
        poseStack.popPose();
    }
}
