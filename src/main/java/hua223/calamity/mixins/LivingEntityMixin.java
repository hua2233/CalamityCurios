package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import hua223.calamity.integration.curios.listeners.CriticalHitTriggerListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.EffectSync;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.IEffectsCallBack;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.Vector2d;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean hurt(@NotNull DamageSource source, float amount);

    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;

    @Shadow public abstract AttributeMap getAttributes();

    @Shadow private boolean effectsDirty;

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

    @Unique
    public float calamity$EffectFragile;

    @Unique
    public byte calamity$InactivationCount;

    @SuppressWarnings("ALL")
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void define(CallbackInfo ci) {
        //For some reason, this seems to only be able to perform initialization in a static block of code
        CalamityHelp.CALAMITY_DATA_SHARED_FLAGS = SynchedEntityData.defineId(LivingEntity.class, CalamityHelp.SHORT);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
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
        boolean result = instance.hasEffect(effect);
        return  calamity$IsPlayer ? result && !CalamityHelp.getCalamityFlag(calamity$Player, 0) : result;
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "HEAD"))
    private void prePenetration(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        calamity$Source = damageSource;
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        shift = At.Shift.AFTER), argsOnly = true, remap = false)
    private DamageSource setCalamity$Source(DamageSource value) {
        return HurtListener.trySetSource(value);
    }

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"))
    private float armorPenetration(float damage, float totalArmor, float toughnessAttribute) {
        if (calamity$Source.getEntity() instanceof ServerPlayer player) {
            float penetrationValue = (float) player.getAttributeValue(CalamityAttributes.ARMOR_PENETRATE.get());
            if (CriticalHitTriggerListener.singlePenetration > 0) {
                //Modify this value during the player's attack and clear the context after it is officially applied,
                //Because Minecraft Server is single threaded。
                penetrationValue += CriticalHitTriggerListener.singlePenetration;
                CriticalHitTriggerListener.singlePenetration = 0;
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
        getEntityData().define(CalamityHelp.CALAMITY_DATA_SHARED_FLAGS, (short) 0);
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

    public final void calamity$ForciblyAddEffect(MobEffectInstance instance, LivingEntity source) {
        MobEffect newEffect = instance.getEffect();
        MobEffectInstance mobeffectinstance = activeEffects.put(newEffect, instance);
        effectsDirty = true;
        if (!level().isClientSide) {
            AttributeMap map = getAttributes();
            int amplifier = instance.getAmplifier();
            if (mobeffectinstance == null) newEffect.addAttributeModifiers(calamity$Entity, map, amplifier);
            else {
                if (newEffect instanceof IEffectsCallBack back) back.onRemove(mobeffectinstance, calamity$Entity);
                newEffect.removeAttributeModifiers(calamity$Entity, map, amplifier);
                newEffect.addAttributeModifiers(calamity$Entity, map, amplifier);
            }

            MinecraftForge.EVENT_BUS.post(new MobEffectEvent.Added(calamity$Entity, mobeffectinstance, instance, source));
            NetMessages.sendToAllClient(new EffectSync(getId(), instance));
        }
    }

    public void calamity$CanClimbable(boolean can) {
        calamity$CanClimbable = can;
    }
}