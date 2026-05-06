package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Calcium extends CalamityEffect {
    public Calcium(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("calcium").setStyle(Style.EMPTY.withColor(9801814)));
    }
}
