package hua223.calamity.mixins;

import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.EffectSync;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
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
import org.objectweb.asm.Opcodes;
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
    @Shadow public abstract AttributeMap getAttributes();

    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;

    @Shadow private boolean effectsDirty;

    @Shadow protected float lastHurt;

    @Unique public boolean calamity$NoMoving;

    @Unique public boolean calamity$EternityLock;

    @Unique
    @OnlyIn(Dist.CLIENT)
    public boolean calamity$GodSlayerFlames;

    @Unique
    public Player calamity$Player;

    @Unique
    public boolean calamity$IsPlayer;

    @Unique
    private LivingEntity calamity$Entity;

    @Unique
    private static DamageSource calamity$Source;

    @Unique
    public float calamity$EffectFragile;

    @Unique
    public byte calamity$InactivationCount;

    @Unique
    public boolean calamity$DisableInvulnerable;

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
        return  calamity$IsPlayer ? result && !calamity$Player.Calamity$Player.hasRadianceEffect : result;
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "HEAD"))
    private void prePenetration(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        calamity$Source = damageSource;
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        shift = At.Shift.AFTER), argsOnly = true, remap = false)//
    private DamageSource setCalamity$Source(DamageSource value) {
        return HurtListener.trySetSource(value);
    }

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"))
    private float armorPenetration(float damage, float totalArmor, float toughnessAttribute) {
        if (calamity$Source.getEntity() instanceof ServerPlayer player) {
            float penetrationValue = (float) player.getAttributeValue(CalamityAttributes.ARMOR_PENETRATE.get());
            if (CriticalHitTriggerListener.singlePenetration > 0) {
                //Modify this value during the tracker's attack and clear the context after it is officially applied,
                //Because Minecraft Server is single threaded。
                penetrationValue += CriticalHitTriggerListener.singlePenetration;
                CriticalHitTriggerListener.singlePenetration = 0;
            }

            if (penetrationValue > 0) {
                totalArmor -= penetrationValue;
                //穿透护甲为负数，韧性最高保持0，否则公式将会在某个临界值扭转它成为免伤，尽管韧性仍为负数
                toughnessAttribute = Math.max(0, toughnessAttribute - penetrationValue / 3);
            }
        }

        calamity$Source = null;
        return CombatRules.getDamageAfterAbsorb(damage, totalArmor, toughnessAttribute);
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    private boolean redirectFireResistanceCheck(LivingEntity instance, MobEffect effect) {
        if (calamity$IsPlayer && !level().isClientSide && calamity$Player.Calamity$Player.calamityCap.isCursePlayer()) return false;
        else return instance.hasEffect(effect);
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 setMove(Vec3 vec3) {
        if (calamity$EternityLock) {
            vec3 = new Vec3(Mth.clamp(vec3.x, -0.04, 0.04),
                Mth.clamp(vec3.y, -0.04, 0.04), Mth.clamp(vec3.z, -0.04, 0.04));
        } else if (calamity$Entity.hasEffect(CalamityEffects.CONFUSED.get())) vec3 = vec3.reverse();
        return vec3;
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target =
        "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F", opcode = Opcodes.PUTFIELD))
    private void disableInvulnerableHurt(LivingEntity instance, float value) {
        lastHurt = calamity$DisableInvulnerable ? lastHurt : value;
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target =
        "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD))
    private void disableInvulnerableTime(LivingEntity instance, int value) {
        invulnerableTime = calamity$DisableInvulnerable ? invulnerableTime : value;
    }

    @Unique
    public void setPos(double x, double y, double z) {
        if (firstTick || !calamity$NoMoving)
            super.setPos(x, y, z);
    }

    @Unique
    public final void calamity$ForciblyAddEffect(MobEffectInstance instance, LivingEntity source) {
        MobEffect newEffect = instance.getEffect();
        MobEffectInstance mobeffectinstance = activeEffects.put(newEffect, instance);
        effectsDirty = true;
        if (!level().isClientSide) {
            AttributeMap map = getAttributes();
            int amplifier = instance.getAmplifier();
            if (mobeffectinstance == null) newEffect.addAttributeModifiers(calamity$Entity, map, amplifier);
            else {
                newEffect.removeAttributeModifiers(calamity$Entity, map, mobeffectinstance.getAmplifier());
                newEffect.addAttributeModifiers(calamity$Entity, map, amplifier);
            }

            MinecraftForge.EVENT_BUS.post(new MobEffectEvent.Added(calamity$Entity, mobeffectinstance, instance, source));

            if (calamity$IsPlayer) NetMessages.sendToTrackingAndSelf(calamity$Player.Calamity$Player.getPlayer(), new EffectSync(getId(), instance));
            else NetMessages.sendToTracking(this, new EffectSync(getId(), instance));
        }
    }
}