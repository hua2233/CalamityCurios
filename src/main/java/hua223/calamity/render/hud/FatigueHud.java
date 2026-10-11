package hua223.calamity.render.hud;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FatigueHud extends EnergyBarHud {
    private static FatigueHud INSTANCE;
    public FatigueHud() {
        super(30);
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
    public float getMaxValue() {
        return 30f;
    }

    @Override
    protected void onTickProgress() {
        color = FastColor.ARGB32.lerp(progress, 0xFF3D0700, 0xFF002600);
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        getInstance().MAIN_TEXTURE = atlas.getSprite(CalamityCurios.ModResource("scales"));
    }
}
