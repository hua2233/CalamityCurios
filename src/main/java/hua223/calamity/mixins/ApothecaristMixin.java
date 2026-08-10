package hua223.calamity.mixins;

import hua223.calamity.util.ITransaction;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist.ApothecaristEntity;
import net.minecraft.world.item.trading.MerchantOffers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ApothecaristEntity.class, remap = false)
public abstract class ApothecaristMixin implements IMerchantWizard {
    @Inject(method = "getOffers", at = @At(value = "FIELD", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD,
        target = "Lio/redspace/ironsspellbooks/entity/mobs/wizards/alchemist/ApothecaristEntity;numberOfRestocksToday:I"))
    private void calamity$Supplier(CallbackInfoReturnable<MerchantOffers> cir) {
        ITransaction.fromTableFill(this);
    }
}
