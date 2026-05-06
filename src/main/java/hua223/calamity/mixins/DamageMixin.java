package hua223.calamity.mixins;

import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageMixin {
    @Unique
    public float calamity$RealAmount;
}
