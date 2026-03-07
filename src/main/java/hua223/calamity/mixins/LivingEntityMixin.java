package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.mixed.ISelfCast;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.Vector2d;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ISelfCast<LivingEntity> {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean hurt(DamageSource source, float amount);

    @Unique
    public Player calamity$Player;
    @Unique
    public boolean calamity$IsPlayer;

    @Unique
    @OnlyIn(Dist.CLIENT)
    private boolean calamity$CanClimbable;
    @Unique
    private LivingEntity calamity$Entity;
    @Unique
    private static DamageSource calamity$Source;
    @Unique
    private Vector2d[] calamity$Offsets;

    @SuppressWarnings("ALL")
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void define(CallbackInfo ci) {
        //For some reason, this seems to only be able to perform initialization in a static block of code
        CalamityHelp.CALAMITY_DATA_SHARED_FLAGS = SynchedEntityData.defineId(LivingEntity.class, CalamityHelp.SHORT);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        LivingEntity entity = cast();
        if (entity instanceof Player player) {
            calamity$IsPlayer = true;
            calamity$Player = player;
        } else {
            calamity$IsPlayer = false;
            calamity$Player = null;
        }

        calamity$Entity = entity;
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", ordinal = 2,
        target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    public boolean injectTravel(LivingEntity instance, MobEffect effect) {
        if (calamity$IsPlayer && calamity$Player.isLocalPlayer()) {
            return instance.hasEffect(effect) && CalamityHelp.getCalamityFlag(calamity$Player, 0);
        }
        return instance.hasEffect(effect);
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "HEAD"))
    private void prePenetration(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        calamity$Source = damageSource;
    }

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"))
    private float armorPenetration(float damage, float totalArmor, float toughnessAttribute) {
        if (calamity$Source.getEntity() instanceof ServerPlayer player) {
            float penetrationValue = (float) player.getAttributeValue(CalamityAttributes.ARMOR_PENETRATE.get());
            if (CriticalHitListener.singlePenetration > 0) {
                //Modify this value during the player's attack and clear the context after it is officially applied,
                //Because Minecraft Server is single threaded。
                penetrationValue += CriticalHitListener.singlePenetration;
                CriticalHitListener.singlePenetration = 0;
            }

            if (penetrationValue > 0) {
                totalArmor -= penetrationValue;
                toughnessAttribute -= penetrationValue / 3;
            }
        }

        calamity$Source = null;
        return CombatRules.getDamageAfterAbsorb(damage, totalArmor, toughnessAttribute);
    }

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    private void defineData(CallbackInfo ci) {
        cast().getEntityData().define(CalamityHelp.CALAMITY_DATA_SHARED_FLAGS, (short) 0);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    private boolean redirectFireResistanceCheck(LivingEntity instance, MobEffect effect) {
        if (calamity$IsPlayer && !calamity$Player.isLocalPlayer()
            && CalamityCap.isCalamity(calamity$Player)) return false;
        else return instance.hasEffect(effect);
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 setMove(Vec3 vec3) {
        if (CalamityHelp.getCalamityFlag(calamity$Entity, 3)) {
            vec3 = new Vec3(Mth.clamp(vec3.x, -0.04, 0.04),
                Mth.clamp(vec3.y, -0.04, 0.04), Mth.clamp(vec3.z, -0.04, 0.04));
        } else if (calamity$Entity.hasEffect(CalamityEffects.CONFUSED.get())) vec3 = vec3.reverse();
        return vec3;
    }

    @Inject(method = "onClimbable", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;",
        shift = At.Shift.AFTER), cancellable = true)
    private void canClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (calamity$CanClimbable && horizontalCollision)
            cir.setReturnValue(true);
    }

    public Vector2d[] calamity$GetPhantomOffset() {
        return calamity$Offsets == null ? calamity$Offsets = new Vector2d[] {new Vector2d(0, 0),
            new Vector2d(0, 0), new Vector2d(0, 0)} : calamity$Offsets;
    }

    @Unique
    public void setPos(double x, double y, double z) {
        if (firstTick || !CalamityHelp.getCalamityFlag(calamity$Entity, 2))
            super.setPos(x, y, z);
    }

    public boolean calamity$HurtNoInvulnerable(DamageSource source, float amount) {
        invulnerableTime = 0;
        return hurt(source, amount);
    }

    public void calamity$CanClimbable(boolean can) {
        calamity$CanClimbable = can;
    }
}