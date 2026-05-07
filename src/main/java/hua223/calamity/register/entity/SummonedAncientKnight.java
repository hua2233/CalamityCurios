package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperAnimatedWarlockAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperRenderer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class SummonedAncientKnight extends KeeperEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_ANIMATING_RISE =
        SynchedEntityData.defineId(SummonedAncientKnight.class, EntityDataSerializers.BOOLEAN);
    private int riseAnimTime;
    private static ServerPlayer globalPlayer;
    private ServerPlayer player;
    private final AnimatableInstanceCache cache;

    public SummonedAncientKnight(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        riseAnimTime = 80;//
        cache = GeckoLibUtil.createInstanceCache(this);
    }

    @SuppressWarnings("ConstantConditions")
    public static void summonedFromDemonGate(DemonGate gate, ServerPlayer player) {
        EntityType<SummonedAncientKnight> type = CalamityEntity.SAK.get();
        Level level = gate.level();
        float radius = 1.5F + 0.185F * 6;
        float angle = (float)Math.PI / 180F;
        globalPlayer = player;
        for (int i = 0; i < 6; i++) {
            SummonedAncientKnight knight = type.create(level);
            float yRot = 6.281F / 6 * i + gate.getYRot() * angle;
            Vec3 spawn = Utils.moveToRelativeGroundLevel(level, gate.position().add(
                new Vec3(radius * Mth.cos(yRot), 0.0F, radius * Mth.sin(yRot))), 10);
            knight.setPos(spawn.x, spawn.y, spawn.z);
            knight.setIsSummoned();
            knight.setIsRestored();
            knight.populateDefaultEquipmentSlots(null, null);
            knight.player = player;
            level.addFreshEntity(knight);
        }

        globalPlayer = null;
        level.playSound(null, gate.getX(), gate.getY(), gate.getZ(), SoundRegistry.RAISE_DEAD_FINISH.get(),
            gate.getSoundSource(), 2.0F, 0.9F + Utils.random.nextFloat() * 0.2F);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new KeeperAnimatedWarlockAttackGoal(
            this, 1.0F, 10, 30));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        targetSelector.addGoal(5, new HurtByTargetGoal(this));
        if (globalPlayer == null) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class , false));
        } else {
            Supplier<Entity> supplier = () -> globalPlayer;
            targetSelector.addGoal(1, new GenericOwnerHurtByTargetGoal(this, supplier));
            targetSelector.addGoal(2, new GenericOwnerHurtTargetGoal(this, supplier));
            targetSelector.addGoal(3, new GenericCopyOwnerTargetGoal(this, supplier));
            targetSelector.addGoal(4, new GenericProtectOwnerTargetGoal(this, supplier));
            goalSelector.addGoal(9, new GenericFollowOwnerGoal(this, supplier,
                0.5, 30.0F, 10.0F, false, 40.0F));
            targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, Mob.class , false));
        }
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_IS_ANIMATING_RISE, true);
    }

    public boolean doHurtTarget(@NotNull Entity entity) {
        return Utils.doMeleeAttack(this, entity, SpellRegistry.RAISE_DEAD_SPELL.get().getDamageSource(this, player));
    }

    public boolean hurt(DamageSource source, float amount) {
        return (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
            !isAnimatingRise() && !shouldIgnoreDamage(source)) && super.hurt(source, amount);
    }

    @Override
    public void tick() {
        if (isAnimatingRise()) {
            if (level().isClientSide) {
                clientDiggingParticles(this);
            }

            if (--riseAnimTime < 0) {
                entityData.set(DATA_IS_ANIMATING_RISE, false);
                setXRot(0.0F);
                setOldPosAndRot();
            }
        } else if (tickCount > 1200) discard();
        else super.tick();
    }

    protected boolean shouldDespawnInPeaceful() {
        return player == null;
    }

    private boolean shouldIgnoreDamage(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !(Boolean) ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get()
            && source.getEntity() != null && DamageSources.isFriendlyFireBetween(source.getEntity(), this);
    }

    @Override
    public void discard() {
        super.discard();
        MagicManager.spawnParticles(level(), ParticleTypes.POOF, getX(), getY(), getZ(),
            25, 0.4, 0.8, 0.4, 0.03, false);
    }

    protected void clientDiggingParticles(LivingEntity livingEntity) {
        RandomSource randomsource = livingEntity.getRandom();
        BlockState blockstate = livingEntity.getBlockStateOn();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            for(int i = 0; i < 15; ++i) {
                double d0 = livingEntity.getX() + (double) Mth.randomBetween(randomsource, -0.5F, 0.5F);
                double d1 = livingEntity.getY();
                double d2 = livingEntity.getZ() + (double)Mth.randomBetween(randomsource, -0.5F, 0.5F);
                livingEntity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), d0, d1, d2, 0.0, 0.0, 0.0);
            }
        }
    }

    public boolean isAnimatingRise() {
        return entityData.get(DATA_IS_ANIMATING_RISE);
    }

    public boolean isPushable() {
        return super.isPushable() && !isAnimatingRise();
    }

    protected boolean isImmobile() {
        return super.isImmobile() || isAnimatingRise();
    }

    public boolean isAlliedTo(Entity entity) {
        return entity instanceof SummonedAncientKnight || (player != null && entity instanceof Player);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.LEGIONNAIRE_FLAMBERGE.get()));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        super.registerControllers(controllerRegistrar);
        controllerRegistrar.add(new AnimationController(this, "rise", 0, this::risePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return tickCount;
    }

    private PlayState risePredicate(AnimationState<?> event) {
        if (!isAnimatingRise()) {
            return PlayState.STOP;
        } else {
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                String animation = (new String[]{"rise_from_ground_01", "rise_from_ground_02",
                    "rise_from_ground_03", "rise_from_ground_04"})[random.nextIntBetweenInclusive(0, 3)];
                event.getController().setAnimation(RawAnimation.begin().thenPlay(animation));
            }

            return PlayState.CONTINUE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends AbstractSpellCastingMobRenderer {
        final KeeperRenderer keeperRenderer;
        public Renderer(EntityRendererProvider.Context renderManager, KeeperRenderer renderer, AbstractSpellCastingMobModel model) {
            super(renderManager, model);
            keeperRenderer = renderer;
        }

        public static Renderer getInstance(EntityRendererProvider.Context renderManager) {
            KeeperRenderer keeperRenderer = new KeeperRenderer(renderManager);
            try {
                Field field = GeoEntityRenderer.class.getDeclaredField("model");
                field.setAccessible(true);
                return new Renderer(renderManager, keeperRenderer, (AbstractSpellCastingMobModel) field.get(keeperRenderer));
            } catch (Exception e) {
                CalamityCurios.LOGGER.error("on get model, unknown compatibility issue has occurred, please report to the developer");
                throw new RuntimeException(e);
            }
        }

        @Override
        public void render(AbstractSpellCastingMob entity, float entityYaw, float partialTick,
                           PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            if (entity.getEntityData().get(DATA_IS_ANIMATING_RISE)) super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            else keeperRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
