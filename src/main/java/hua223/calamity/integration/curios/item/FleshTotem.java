package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FleshTotem extends BaseCurio {
    public FleshTotem(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ItemCooldowns cooldowns = listener.player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) return;

        listener.amplifier -= 0.95f;
        cooldowns.addCooldown(this, 200);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("flesh_totem"));
        return tooltips;
    }
}
