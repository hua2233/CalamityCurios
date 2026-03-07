package hua223.calamity.render.entity;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;

@OnlyIn(Dist.CLIENT)
public class CrystallizationRenderLayer {
    private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/gui/crystallization.png");
    private static final float R_B = 187f / 255f;
    private static final float GREEN = 110f / 255f;
    public static boolean canRender = false;
    private static float alpha = 1f;
    private static boolean startColorChange = false;
    private static boolean isFirstHalf = true;
    private static long lastGameTick = 0;
    private static boolean nonstop = false;

    public static void renderTextureOverlay(RenderGuiEvent.Post event) {
        if (canRender) {
            Window window = event.getWindow();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            if (startColorChange) {
                long tick = Minecraft.getInstance().player.tickCount;
                if (tick >= lastGameTick) {
                    lastGameTick = tick + 5;
                    RenderSystem.setShaderColor(R_B, GREEN, R_B, changeAlpha());
                } else {
                    RenderSystem.setShaderColor(R_B, GREEN, R_B, alpha);
                }
            } else {
                RenderSystem.setShaderColor(R_B, GREEN, R_B, alpha);
            }

            RenderSystem.setShaderTexture(0, TEXTURE);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(0.0D, window.getGuiScaledHeight(), -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(window.getGuiScaledWidth(), window.getGuiScaledHeight(), -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(window.getGuiScaledWidth(), 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static float changeAlpha() {
        alpha -= 0.05f;
        if (isFirstHalf) {
            if (alpha <= 0.5f) {
                if (nonstop) {
                    nonstop = false;
                } else {
                    startColorChange = false;
                }
                isFirstHalf = false;
            }
        } else if (alpha <= 0) stop();

        return alpha;
    }

    public static void startChange() {
        if (startColorChange) {
            notStopChange();
        } else startColorChange = true;
    }

    public static void stop() {
        startColorChange = false;
        isFirstHalf = true;
        canRender = false;
    }

    public static void notStopChange() {
        nonstop = true;
        startColorChange = true;
    }

    public static void start() {
        if (canRender) return;
        alpha = 1f;
        canRender = true;
    }
}
