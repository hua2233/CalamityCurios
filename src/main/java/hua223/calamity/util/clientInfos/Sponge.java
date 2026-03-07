package hua223.calamity.util.clientInfos;

import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sponge {
    public static int spongeProgress = 0;
    private static float lastProgress;
    private static float finalProgress;
    private static byte tickProgress;
    public static boolean notRenderSponge = true;

    public static void setSpongeProgress(float value) {
        lastProgress = spongeProgress / 150f;
        finalProgress = Mth.clamp(value / 30f, 0f, 1f);

        if (!DelayRunnable.addUniqueLoopTask(() -> {
            tickProgress++;

            if (tickProgress >= 20) {
                tickProgress = 0;
                spongeProgress = (int) (150 * finalProgress);
                return true;
            } else {
                float current = Mth.lerp(tickProgress / 20f, lastProgress, finalProgress);
                spongeProgress = (int) (150 * current);
                return false;
            }
        }, 1, Sponge.class)) {
            tickProgress = 0;
            DelayRunnable.setTaskTime(Sponge.class, 0);
        }
    }

    public static void startSpongeAndInit() {
        spongeProgress = 0;
        notRenderSponge = false;
    }

    public static void closeSponge() {
        notRenderSponge = true;
        tickProgress = 0;
        DelayRunnable.removeTask(Sponge.class);
    }
}
