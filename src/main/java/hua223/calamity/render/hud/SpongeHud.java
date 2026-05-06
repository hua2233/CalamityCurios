package hua223.calamity.render.hud;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpongeHud extends EnergyBarHud {
    private static SpongeHud INSTANCE;
    //最近有个游戏，剧情搞得我很难受。我不怎骂人，但确实太抽象了，我靠作者你********。
    //就是莫名难受，我可能确实多愁善感。唉，难受，睡觉去。

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
