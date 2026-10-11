package hua223.calamity.mixed;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ICalamityHeartType {
    int calamity$GetX(boolean halfHeart, boolean renderHighlight);

    int calamity$GetY(int y);
}
