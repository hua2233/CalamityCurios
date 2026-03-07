package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
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
    public final void onHurt(HurtListener listener) {
        listener.amplifier += 0.2f;
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        listener.amplifier += 0.4f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("dimensional"));
        return tooltips;
    }
}
