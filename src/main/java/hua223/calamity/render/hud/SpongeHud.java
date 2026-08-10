package hua223.calamity.render.hud;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpongeHud extends EnergyBarHud {
    private static SpongeHud INSTANCE;

    public SpongeHud() {
        super(0, 30);
        INSTANCE = this;
        color = 0xFF6FD2F3;
    }

    @Override
    protected void renderMain(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(x, y, 0, 54, 54, MAIN_TEXTURE);
    }

    public static SpongeHud getInstance() {
        return INSTANCE;
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        getInstance().MAIN_TEXTURE = atlas.getSprite(CalamityCurios.ModResource("sponge"));
    }
}
