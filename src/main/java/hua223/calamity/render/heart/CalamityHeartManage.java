package hua223.calamity.render.heart;

import hua223.calamity.mixed.ICalamityHeartType;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CalamityHeartManage {
    public static int astrAmount;

    public static ICalamityHeartType astr;

    private static ICalamityHeartType renderHeart;

    public static void setFromLifeFruitLevel(int level) {
        renderHeart = createNewHeartType(106 + level * 36);
    }

    public static void setAstrHeart(int value) {
        astrAmount = value;
        if (value > 0 && astr == null)
            astr = createNewHeartType(70);
        else if (value <= 0) astr = null;
    }

    public static ICalamityHeartType forPlayer(Player player) {
        Object type;
        if (player.hasEffect(MobEffects.POISON))  type = Gui.HeartType.POISIONED;
        else if (player.hasEffect(MobEffects.WITHER)) type = Gui.HeartType.WITHERED;
        else if (player.isFullyFrozen()) type = Gui.HeartType.FROZEN;
        else if (renderHeart != null) return renderHeart;
        else type = Gui.HeartType.NORMAL;

        return (ICalamityHeartType) type;
    }

    private static ICalamityHeartType createNewHeartType(final int baseX) {
        return new ICalamityHeartType() {
            @Override
            public int calamity$GetX(boolean halfHeart, boolean renderHighlight) {
                int x = baseX;
                if (halfHeart) x += 9;
                if (renderHighlight) x += 18;
                return x ;
            }

            @Override
            public int calamity$GetY(int y) {
                return 18;
            }
        };
    }
}
