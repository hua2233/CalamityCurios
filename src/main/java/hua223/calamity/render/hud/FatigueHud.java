package hua223.calamity.render.hud;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FatigueHud extends EnergyBarHud {
    private static FatigueHud INSTANCE;
    public FatigueHud() {
        super(30, 100);
        INSTANCE = this;
    }

    public static FatigueHud getInstance() {
        return INSTANCE;
    }

    @Override
    protected void renderMain(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(x, y - 45, 0, 54, 54, MAIN_TEXTURE);
    }

    @Override
    public void setProgress(float value) {
        lastProgress = progress;
        finalProgress = value / maxValue;
        if (!DelayRunnable.addUniqueLoopTask(() -> {
            frameProgress = progress;

            color = FastColor.ARGB32.lerp(progress, 0xFF3D0700, 0xFF002600);
            progress = Mth.clamp(Mth.lerp(++tickProgress / 20f, lastProgress, finalProgress), 0f, 1f);
            if (progress == finalProgress) {
                tickProgress = 0;
                return true;
            }
            return false;
        }, 1, getClass())) tickProgress = 0;
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        getInstance().MAIN_TEXTURE = atlas.getSprite(CalamityCurios.ModResource("scales"));
    }
}
