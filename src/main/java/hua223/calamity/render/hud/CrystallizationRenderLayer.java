package hua223.calamity.render.hud;

import hua223.calamity.events.LogoutRelease;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
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
        } else if (alpha <= 0) stop(null);

    }

    public static void startChange() {
        if (startColorChange) {
            notStopChange();
        } else {
            startColorChange = true;
            lastGameTick = RenderUtil.getLocalTick();
        }

    }

    @LogoutRelease
    public static void stop(LocalPlayer player) {
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
}
