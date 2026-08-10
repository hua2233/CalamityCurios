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
public class SeraphTracers extends AngelTreads {
    public SeraphTracers(Properties properties) {
        super(properties);
    }

    @Override
    protected float getMaxAcceleration() {
        return 0.5f;
    }

    @Override
    protected int getFlyTime() {
        return 600;
    }

    @Override
    protected float getVerticalSpeed() {
        return 3.5f;
    }

    @Override
    protected float getFlySpeedAmplifier() {
        return 1.3f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void addTooltip(List<Component> tooltips, Style defaultStyle) {
        tooltips.add(CMLangUtil.getTranslatable("seraph_tracers").setStyle(defaultStyle));
    }
}
