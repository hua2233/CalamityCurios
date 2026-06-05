package hua223.calamity.mixins;

import hua223.calamity.util.damage.DamageTags;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpellDamageSource.class)
public class SpellDamageMixin extends DamageSource {
    public SpellDamageMixin(Holder<DamageType> type) {
        super(type);
    }

    @Override
    public boolean is(@NotNull TagKey<DamageType> key) {
        return key == DamageTags.CALAMITY_MAGIC.tag || super.is(key);
    }
}
