package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SacrificesMask extends BaseCurio {
    public SacrificesMask(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.source.getDirectEntity() == listener.player &&
            listener.player.getHealth() < listener.player.getMaxHealth())
            listener.player.heal(listener.baseAmount * 0.15f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("sacrifices_mask").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
