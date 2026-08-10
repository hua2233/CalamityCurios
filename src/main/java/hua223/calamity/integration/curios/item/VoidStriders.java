package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(Wings.class)
public class VoidStriders extends AngelTreads {
    public VoidStriders(Properties properties) {
        super(properties);
    }

    @Override
    protected float getMaxAcceleration() {
        return 0.36f;
    }

    @Override
    protected int getFlyTime() {
        return 400;
    }

    @Override
    protected float getVerticalSpeed() {
        return 2.7f;
    }

    @Override
    protected float getFlySpeedAmplifier() {
        return 1.23f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void addTooltip(List<Component> tooltips, Style defaultStyle) {
        tooltips.add(CMLangUtil.getTranslatable("void_striders").setStyle(defaultStyle));
    }
}
