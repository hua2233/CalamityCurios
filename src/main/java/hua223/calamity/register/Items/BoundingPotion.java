package hua223.calamity.register.Items;

import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class BoundingPotion extends CalamityPotion implements IDataPackResponse {
    public BoundingPotion(Properties properties, String text, Supplier<MobEffectInstance> supplier) {
        super(properties, text, supplier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().jumpPower += tag.getFloat("bounding");
    }
}
