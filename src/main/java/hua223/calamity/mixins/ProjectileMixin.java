package hua223.calamity.mixins;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.OutlineDetected;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Projectile.class})
public abstract class ProjectileMixin extends Entity {
    @Unique
    public boolean calamity$Indestructible;

    @Unique
    @OnlyIn(Dist.CLIENT)
    public boolean calamity$Detected;

    public ProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setOwner", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/Entity;getUUID()Ljava/util/UUID;", shift = At.Shift.AFTER))
    private void setData(Entity owner, CallbackInfo ci) {
        NetMessages.sendToAllClient(new OutlineDetected(this));
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/nbt/CompoundTag;getUUID(Ljava/lang/String;)Ljava/util/UUID;", shift = At.Shift.AFTER))
    private void loadSetData(CompoundTag compound, CallbackInfo ci) {
        NetMessages.sendToAllClient(new OutlineDetected(this));
    }

    @Unique
    public void discard() {
        if (calamity$Indestructible) {
            calamity$Indestructible = false;
        } else super.discard();
    }
}
