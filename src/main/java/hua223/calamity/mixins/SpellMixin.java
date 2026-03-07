package hua223.calamity.mixins;

import hua223.calamity.mixed.ICalamityMagicExpand;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class SpellMixin {
    @Shadow public abstract int getManaCost(int level);

    @Unique
    private static MagicData calamity$TempData;
    @Unique
    private static int calamity$SpellLevel;
    @Inject(method = "canBeCastedBy", at = @At("HEAD"))
    private void getTemp(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        calamity$TempData = playerMagicData;
        calamity$SpellLevel = spellLevel;
    }

    @ModifyVariable(method = "canBeCastedBy", at = @At(value = "STORE"), ordinal = 0)
    private boolean canCasted(boolean value) {
        return value || ((ICalamityMagicExpand) calamity$TempData).calamity$UsePotionMana(getManaCost(calamity$SpellLevel), true);
    }
}
