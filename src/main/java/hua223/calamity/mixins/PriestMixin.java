package hua223.calamity.mixins;

import hua223.calamity.util.ITransaction;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import io.redspace.ironsspellbooks.entity.mobs.wizards.priest.PriestEntity;
import net.minecraft.world.item.trading.MerchantOffers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PriestEntity.class, remap = false)
public abstract class PriestMixin implements IMerchantWizard {
    @Inject(method = "getOffers", at = @At(value = "FIELD", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD,
        target = "Lio/redspace/ironsspellbooks/entity/mobs/wizards/priest/PriestEntity;numberOfRestocksToday:I"))
    private void calamity$Supplier(CallbackInfoReturnable<MerchantOffers> cir) {
        ITransaction.fromTableFill(this);
    }
}
