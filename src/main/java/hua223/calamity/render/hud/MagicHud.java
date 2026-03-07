package hua223.calamity.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.clientInfos.Magic;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class MagicHud implements IGuiOverlay {
    private static final ResourceLocation MAGIC_TEXTURE = CalamityCurios.ModResource("textures/hud/mana.png");
    private static final ResourceLocation COMET_SHARD = CalamityCurios.ModResource("textures/hud/comet_shard.png");
    private static final ResourceLocation ETHEREAL_CORE = CalamityCurios.ModResource("textures/hud/ethereal_core.png");
    private static final ResourceLocation PHANTOM_HEART = CalamityCurios.ModResource("textures/hud/phantom_heart.png");

    @Override
    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        int currentMana = Magic.magicValue;
        if (currentMana == 0) return;

        int x = screenHeight + 180;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        setLevelTexture(Magic.level);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        for (int i = 0; currentMana > 0; currentMana -= 50, i++) {
            if (currentMana >= 50) {
                GuiComponent.blit(poseStack, x, (i * 10), 0, 0, 10, 10, 10, 10);
            } else {
                setZoomScale(currentMana, poseStack, x, i);
            }
        }
    }

    private static void setLevelTexture(int level) {
        switch (level) {
            case 1 -> RenderSystem.setShaderTexture(0, COMET_SHARD);
            case 2 -> RenderSystem.setShaderTexture(0, ETHEREAL_CORE);
            case 3 -> RenderSystem.setShaderTexture(0, PHANTOM_HEART);
            default -> RenderSystem.setShaderTexture(0, MAGIC_TEXTURE);
        }
    }

    private static void setZoomScale(int value, PoseStack poseStack, int x, int y) {
        float scale = 1f;
        float XOffset = 1f;
        if (value < 50 && value >= 40) {
            scale = 0.9f;
            XOffset = 0.75f;
        } else if (value < 40 && value >= 30) {
            scale = 0.8f;
            XOffset = 1.4f;//
        } else if (value < 30 && value >= 20) {
            scale = 0.7f;
            XOffset = 2.7f;
        } else if (value < 20 && value >= 10) {
            scale = 0.6f;
            XOffset = 3f;//
        } else if (value < 10 && value > 0) {
            scale = 0.5f;
            XOffset = 4.5f;
        }
        zoomTexture(scale, XOffset, poseStack, x, y);
    }

    private static void zoomTexture(float scale, float XOffset, PoseStack poseStack, int x, int y) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1f);
        poseStack.translate(XOffset, 0, 0);
        GuiComponent.blit(poseStack,
            (int) (x / scale),
            (int) (y * 10 / scale),
            0, 0, 10, 10, 10, 10);
        poseStack.popPose();
    }
}
