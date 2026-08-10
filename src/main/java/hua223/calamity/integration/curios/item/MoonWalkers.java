package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(Wings.class)
public class MoonWalkers extends AngelTreads {
    public MoonWalkers(Properties properties) {
        super(properties);
    }

    @Override
    protected float getMaxAcceleration() {
        return 0.28f;
    }

    @Override
    protected int getFlyTime() {
        return 300;
    }

    @Override
    protected float getFlySpeedAmplifier() {
        return 1.18f;
    }

    @Override
    protected float getVerticalSpeed() {
        return 2.3f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void addTooltip(List<Component> tooltips, Style defaultStyle) {
        tooltips.add(CMLangUtil.getTranslatable("moon_walkers").setStyle(defaultStyle));
    }
}
