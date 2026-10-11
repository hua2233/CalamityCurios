package hua223.calamity.render.item;

import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDraedonCreationBG extends ICustomBackgroundRender, IBackwardsCompatDefaultNbtItem {
    @Override
    @OnlyIn(Dist.CLIENT)
    default RenderType getRenderType() {
        return CalamityShaders.getDisintegration();
    }

    @Override
    default void setupItem(ItemStack itemStack) {
        itemStack.getOrCreateTag().putInt(CalamityHelp.FONT_FLAG, 5);
    }
}
