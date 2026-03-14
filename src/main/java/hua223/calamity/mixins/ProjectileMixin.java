package hua223.calamity.mixins;

import hua223.calamity.util.CalamityHelp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin({Projectile.class})
public abstract class ProjectileMixin extends Entity {
    @Shadow @Nullable private UUID ownerUUID;
    @Unique
    public boolean calamity$Indestructible;

    public ProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    static {
        CalamityHelp.CALAMITY_PROJECTILE_TAG = SynchedEntityData.defineId(Projectile.class, EntityDataSerializers.BOOLEAN);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void defindData(EntityType entityType, Level level, CallbackInfo ci) {
        getEntityData().define(CalamityHelp.CALAMITY_PROJECTILE_TAG, false);
    }

    @Inject(method = "setOwner", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/Entity;getUUID()Ljava/util/UUID;", shift = At.Shift.AFTER))
    private void setData(Entity owner, CallbackInfo ci) {
        getEntityData().set(CalamityHelp.CALAMITY_PROJECTILE_TAG, owner instanceof Enemy);
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/nbt/CompoundTag;getUUID(Ljava/lang/String;)Ljava/util/UUID;", shift = At.Shift.AFTER))
    private void loadSetData(CompoundTag compound, CallbackInfo ci) {
        getEntityData().set(CalamityHelp.CALAMITY_PROJECTILE_TAG, ((ServerLevel) level).getEntity(ownerUUID) instanceof Enemy);
    }

    @Unique
    public void discard() {
        if (calamity$Indestructible) {
            calamity$Indestructible = false;
        } else super.discard();
    }
}
