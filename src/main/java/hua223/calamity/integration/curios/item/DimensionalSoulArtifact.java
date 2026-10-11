package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PreAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DimensionalSoulArtifact extends BaseCurio {
    public DimensionalSoulArtifact(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PreAttackListener listener) {
        listener.amplifier += .4f;
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        listener.amplifier += 0.2f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("dimensional", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("dimensional", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
