package hua223.calamity.register.Items;

import hua223.calamity.util.RenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class BrimstoneLocus extends TooltipItem {
    public BrimstoneLocus(Properties properties, String name, int lien) {
        super(properties, name, lien);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LocalPlayer player && !player.isCreative())
            RenderUtil.renderGuiEnchantParticle = true;
    }
}
