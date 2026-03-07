package hua223.calamity.util.clientInfos;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Rage {
    public static boolean rageEnabled;
    public static boolean animationFrameTime;
    public static int ShatteredLevel;
    public static int rageProgress;
    public static boolean hasRageItem;
    public static int rageItemCount;
    public static int levelUpProgress;
    public static int levelBonus;
    private static int currentDamage;
    private static double levelUpDamage = 100;

    public static void setRageProgress(float rageValue) {
        rageProgress = (int) ((rageValue / 100f) * 80);
    }

    public static void setRageCount(int count) {
        rageItemCount = count;
        if (count > 0) hasRageItem = true;
    }

    private static int getLevelUpProgress() {
        return (int) ((currentDamage / levelUpDamage) * 100);
    }

    public static void setCurrentDamage(int damage) {
        currentDamage = damage;
        levelUpProgress = getLevelUpProgress();
    }

    public static void setLevelUpDamage(int damage) {
        levelUpDamage = damage;
        levelUpProgress = getLevelUpProgress();
    }

    private static int getLevelBonus() {
        return 35 + ShatteredLevel * 2;
    }

    public static void setLevel(int level) {
        ShatteredLevel = level;
        levelBonus = getLevelBonus();
    }
}
