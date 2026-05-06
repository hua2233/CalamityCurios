package hua223.calamity.render.entity;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class CrystallizationRenderLayer implements IGuiOverlay {
    private static TextureAtlasSprite TEXTURE;
    private static final float R_B = 187f / 255f;
    private static final float GREEN = 110f / 255f;
    private static boolean canRender = false;
    private static float alpha = 1f;
    private static boolean startColorChange = false;
    private static boolean isFirstHalf = true;
    private static int lastGameTick = 0;
    private static boolean nonstop = false;

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {
        if (canRender) {
            if (startColorChange) {
                short tick = RenderUtil.getLocalTick();
                if (tick >= lastGameTick) {
                    lastGameTick = ((tick + 5) % 3600);
                    changeAlpha();
                }
            }

            guiGraphics.setColor(R_B, GREEN, R_B, alpha);
            guiGraphics.blit(0, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), TEXTURE);
        }
    }

    private static void changeAlpha() {
        alpha -= 0.05f;
        if (isFirstHalf) {
            if (alpha <= 0.5f) {
                if (nonstop) nonstop = false;
                else startColorChange = false;
                isFirstHalf = false;
            }
        } else if (alpha <= 0) stop();

    }

    public static void startChange() {
        if (startColorChange) {
            notStopChange();
        } else {
            startColorChange = true;
            lastGameTick = RenderUtil.getLocalTick();
        }

    }

    public static void stop() {
        startColorChange = false;
        isFirstHalf = true;
        lastGameTick = 0;
        nonstop = false;
        canRender = false;
    }

    public static void start() {
        canRender = true;
        alpha = 1f;
    }

    public static void notStopChange() {
        nonstop = true;
        startColorChange = true;
        lastGameTick = RenderUtil.getLocalTick();
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        TEXTURE = atlas.getSprite(CalamityCurios.ModResource("crystallization"));
    }

    //    @Deprecated
//    public static void renderTextureOverlay(RenderGuiEvent.Post event) {
//        if (canRender) {
//            Window window = event.getWindow();
//            RenderSystem.disableDepthTest();
//            RenderSystem.depthMask(false);
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//
//            if (startColorChange) {
//                long tick = Minecraft.getInstance().player.tickCount;
//                if (tick >= lastGameTick) {
//                    lastGameTick = tick + 5;
//                    RenderSystem.setShaderColor(R_B, GREEN, R_B, changeAlpha());
//                } else {
//                    RenderSystem.setShaderColor(R_B, GREEN, R_B, alpha);
//                }
//            } else {
//                RenderSystem.setShaderColor(R_B, GREEN, R_B, alpha);
//            }
//
//            RenderSystem.setShaderTexture(0, TEXTURE);
//            Tesselator tesselator = Tesselator.getInstance();
//            BufferBuilder bufferbuilder = tesselator.getBuilder();
//            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//            bufferbuilder.vertex(0.0D, window.getGuiScaledHeight(), -90.0D).uv(0.0F, 1.0F).endVertex();
//            bufferbuilder.vertex(window.getGuiScaledWidth(), window.getGuiScaledHeight(), -90.0D).uv(1.0F, 1.0F).endVertex();
//            bufferbuilder.vertex(window.getGuiScaledWidth(), 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
//            bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
//            tesselator.end();
//            RenderSystem.depthMask(true);
//            RenderSystem.enableDepthTest();
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        }
//    }
}
