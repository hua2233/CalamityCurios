package hua223.calamity.render.hud;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FatigueHuds extends EnergyBarHud {
//    private static final int INNER_RING_START_COLOR = 0xCC1900;
//    private static final int INNER_RING_END_COLOR = 0x008000;
//    private static final int OUTER_RING_START_COLOR = 0x3D0700;
//    private static final int OUTER_RING_END_COLOR = 0x002600;
    private static FatigueHuds INSTANCE;
    protected FatigueHuds() {
        super(30, 100);
        INSTANCE = this;
    }

    public static FatigueHuds getInstance() {
        return INSTANCE;
    }

    @Override
    public void setProgress(float value) {
        super.setProgress(value);
        color = Mth.lerp(Mth.clamp(value / maxValue, 0f, 1f), , 3999488);
    }
}
