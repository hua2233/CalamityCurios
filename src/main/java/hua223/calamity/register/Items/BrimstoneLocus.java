package hua223.calamity.register.Items;

import hua223.calamity.util.RenderUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BrimstoneLocus extends TooltipItem {
    public BrimstoneLocus(Properties properties, String name, int lien) {
        super(properties, name, lien);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        RenderUtil.renderGuiEnchantParticle = true;
    }
}
