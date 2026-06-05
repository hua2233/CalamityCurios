package hua223.calamity.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class FatigueSlot implements IGuiOverlay {
    private static TextureAtlasSprite TEXTURE;
    private static TextureAtlasSprite BAR;
    public static boolean notRender = true;
    private static float progress = 1f;
    public static void setProgress(int value) {
        progress = value / 100f;
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (notRender) return;
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        PoseStack stack = guiGraphics.pose();

        stack.pushPose();
        stack.scale(0.4f, 0.4f, 1f);
        int x = screenWidth + 350;
        int y = screenHeight + 330;
        guiGraphics.blit(x, y, 0, 34, 70, TEXTURE);
        float v = BAR.getV0();
        guiGraphics.innerBlit(BAR.atlasLocation(), x + 14, x + 20, y + 18,
            y + (int) (52 * (progress)), 0, BAR.getU0(), BAR.getU1(), v,  v + (BAR.getV1() - v) * progress);
        stack.popPose();
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        TEXTURE = atlas.getSprite(CalamityCurios.ModResource("fatigue_border"));
        BAR = atlas.getSprite(CalamityCurios.ModResource("fatigue_bar"));
    }
}
