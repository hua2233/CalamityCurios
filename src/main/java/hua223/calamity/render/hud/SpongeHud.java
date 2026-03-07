package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.clientInfos.Sponge;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class SpongeHud implements IGuiOverlay {
    private static final ResourceLocation SPONGE = CalamityCurios.ModResource("textures/hud/sponge_hud.png");
    private static final ResourceLocation SPONGE_TEXTURE = CalamityCurios.ModResource("textures/hud/sponge.png");

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (Sponge.notRenderSponge) return;
        int x = screenWidth / 2;
        int y = screenHeight / 2;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, SPONGE);

        poseStack.pushPose();
        poseStack.scale(0.6f, 0.5f, 1f);
        poseStack.translate(-200, 350f, 0);
        GuiComponent.blit(poseStack, x, y, 0, 0, 175, 20, 325, 20);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.scale(0.6f, 0.5f, 1f);
        poseStack.translate(-178, 352f, 0);
        GuiComponent.blit(poseStack, x, y, 175, 0, Sponge.spongeProgress, 16, 325, 20);
        poseStack.popPose();

        RenderSystem.setShaderTexture(0, SPONGE_TEXTURE);
        poseStack.pushPose();
        poseStack.scale(0.2f, 0.2f, 1f);
        poseStack.translate(-118, 1075f, 0);
        GuiComponent.blit(poseStack, x, y, 0, 0, 54, 54, 54, 54);
        poseStack.popPose();
    }
}
