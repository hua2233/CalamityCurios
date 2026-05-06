package hua223.calamity.register.Items;

import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.RenderUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class OmnisciencePotion extends CalamityPotion implements IDataPackResponse {
    public OmnisciencePotion(Properties properties, String text, Supplier<MobEffectInstance> supplier) {
        super(properties, text, supplier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        RenderUtil.Shaders.renderHighlightBlocks(false);
    }
}
