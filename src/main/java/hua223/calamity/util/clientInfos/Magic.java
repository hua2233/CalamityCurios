package hua223.calamity.util.clientInfos;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Magic {
    public static volatile int magicValue;
    public static volatile int level;
    public static volatile boolean notEnabled;
}
