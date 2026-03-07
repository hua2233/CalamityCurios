package hua223.calamity.util.clientInfos;

import hua223.calamity.render.hud.AdrenalineHud;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Adrenaline {
    public static boolean adrenalinEnabled;
    public static boolean isNanoMachinesMode = true;
    public static boolean isAdrenalineAnimation;
    public static boolean isNanoAnimation;
    public static boolean hasAdrenalineItem;
    public static int adrenalineItemCount;
    private static int adrenalineProgress;

    public static void setForMachinesMode(boolean isNano) {
        isNanoMachinesMode = isNano;
    }

    public static void playAnimation(boolean play) {
        isAdrenalineAnimation = play;
        if (isNanoMachinesMode) setNanoAnimation(play);
    }

    public static void setNanoAnimation(boolean offOrOn) {
        AdrenalineHud.lastNanoTick = 0;
        AdrenalineHud.nanoTick = 0;
        isNanoAnimation = offOrOn;
    }

    public static void setAdrenalineCount(int count) {
        adrenalineItemCount = count;
        if (count > 0) hasAdrenalineItem = true;
    }

    public static int getAdrenalineProgress() {
        return adrenalineProgress;
    }

    public static void setAdrenalineProgress(int value) {
        adrenalineProgress = (int) ((value / 30f) * 80);//Math.min(, 80);
    }

    public static void setAdrenalineEnabled(boolean offOrOn) {
        adrenalinEnabled = offOrOn;
    }
}
