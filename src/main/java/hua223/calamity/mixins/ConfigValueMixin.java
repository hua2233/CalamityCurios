package hua223.calamity.mixins;

import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ForgeConfigSpec.ConfigValue.class)
public class ConfigValueMixin {
    @Unique
    public Object[] calamity$According;
}
