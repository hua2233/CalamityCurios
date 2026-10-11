package hua223.calamity.render.hud;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BarrierHud extends EnergyBarHud {
    private static BarrierHud INSTANCE;

    public BarrierHud() {
        super(0);
        INSTANCE = this;
        color = 0xFF6FD2F3;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public float getMaxValue() {
        return CalamityHelp.getClientCalamity().shieldDurabilityMax;
    }

    @Override
    protected void renderMain(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(x + 8, y + 6, 0, 40, 40, MAIN_TEXTURE);
    }

    public static BarrierHud getInstance() {
        return INSTANCE;
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        INSTANCE.MAIN_TEXTURE = atlas.getSprite(CalamityCurios.ModResource("barrier"));
    }
}
