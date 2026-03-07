package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ProjectileHitListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DaawnlightSpiritOrigin extends BaseCurio {
    public DaawnlightSpiritOrigin(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onProjectileHit(ProjectileHitListener listener) {
        double hit = listener.projectile.getY();
        double eye = listener.target.getEyeY();
        if (Math.abs(Math.abs(hit) - Math.abs(eye)) < 0.2)
            listener.setDaawnlight();
            //listener.projectile.getTags().add("daawnlight");

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("daawnlight", 2));
            tooltips.add(CMLangUtil.getTranslatable("daawnlight", 3));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("daawnlight", 1));
        }
        return super.getSlotsTooltip(tooltips, stack);
    }
}
