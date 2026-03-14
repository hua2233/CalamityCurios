package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.mixed.ICalamityMagicExpand;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import hua223.calamity.util.primitive.PrimitiveRenderer;
import hua223.calamity.util.primitive.PrimitiveSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.ThreadLocalRandom;

public class EternityHex extends Entity {
    //Approximately 5.15s in Terraria. 310 frames.
    //This is because Minecraft uses Tick instead of maxFrame rate. Balance using a setting of 60 frames for 20tick。
    private static final int LIFE_TIME = 206;
    private LivingEntity target;
    private Player owner;
    private ICalamityMagicExpand expand;
    private float yOffset;

    @OnlyIn(Dist.CLIENT)
    private final Vector4f finalColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private final Vector4f tailColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private float alpha = 1f;
    @OnlyIn(Dist.CLIENT)
    private static final Vector4f HEAD_COLOR =
        RenderUtil.interpolateColor(RenderUtil.black(), RenderUtil.MAGENTA, 0.1f, null);
    @OnlyIn(Dist.CLIENT)
    private static final float ANGLE = Mth.TWO_PI / 200f;
    @OnlyIn(Dist.CLIENT)
    private final CircleBuffer<Vec3> oldPos = new CircleBuffer<>(64);
    @OnlyIn(Dist.CLIENT)
    private float maxFrame;
    @OnlyIn(Dist.CLIENT)
    private int framerateLimit;
    @OnlyIn(Dist.CLIENT)
    private float partialLimit;
    @OnlyIn(Dist.CLIENT)
    private float lemniscateAngle;
    @OnlyIn(Dist.CLIENT)
    private final Vector2d lemniscateOffset = new Vector2d(0, 0);
    @OnlyIn(Dist.CLIENT)
    private int currentFrame;
    @OnlyIn(Dist.CLIENT)
    private float extraUpdate;
    @OnlyIn(Dist.CLIENT)
    private final Vector3d pos = new Vector3d(0, 0, 0);
    @OnlyIn(Dist.CLIENT)
    private final PrimitiveSettings settings = new PrimitiveSettings(
        completionRatio -> {
            float widthInterpolant = RenderUtil.clampLerp(0f, 0.12f, completionRatio, true);
            return RenderUtil.smoothStep(0.02f, 0.25f, widthInterpolant);
        },
        completionRatio -> {
            float leftoverTimeScale = (float) Math.sin((double) RenderUtil.getLocalTick() / 15) * 0.5f + 0.5f;
            leftoverTimeScale *= 0.5f;

            RenderUtil.interpolateColor(RenderUtil.MAGENTA, RenderUtil.CYAN, completionRatio * 0.5f + leftoverTimeScale, tailColor);

            float opacity = (float) Math.pow(RenderUtil.clampLerp(1f, 0.61f, completionRatio, true), 0.4) * alpha;
            float fadeToMagenta = RenderUtil.smoothStep(0f, 1f, (float) Math.pow(completionRatio, 0.6d));
            return RenderUtil.multiplyColor(RenderUtil.interpolateColor(HEAD_COLOR, tailColor, fadeToMagenta, finalColor), opacity, finalColor);
        }, true, 40, 3,
        RenderUtil.Shaders.getLemniscateRenderType(CalamityCurios.ModResource("textures/entity/eternity_streak.png")));

    public EternityHex(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        if (level.isClientSide) {
            int framerate = Minecraft.getInstance().getWindow().getFramerateLimit();
            maxFrame = framerate * 2;
            //In Terraria, this is 180 updates per second, so it should be averaged here ^~^
            float f = 180f / framerate;
            framerateLimit = (int) f;
            partialLimit = f - framerateLimit;
        }
    }

    public static void create(Player player, Level level, LivingEntity target) {
        CalamityCapProvider.safetyRunCalamityMagic(player, expand -> {
            if (expand.calamity$GetMana() > 30) {
                EternityHex hex = CalamityEntity.ETERNITY_HEX.get().create(level);
                if (hex != null) {
                    hex.yOffset = target.getBbHeight() / 2;
                    hex.expand = expand;
                    hex.setPos(target.position().add(0, hex.yOffset, 0));
                    hex.owner = player;
                    hex.target = target;
                    CalamityHelp.setCalamityFlag(target, 3, true);
                    level.addFreshEntity(hex);
                }
            } else player.stopUsingItem();
        });
    }

    private boolean reSpawn() {
        if (canAlive() && (!target.isDeadOrDying() || chooseNewTarget())) {
            EternityHex hex = CalamityEntity.ETERNITY_HEX.get().create(level);
            hex.yOffset = yOffset;
            hex.expand = expand;
            hex.setPos(target.position().add(0, yOffset, 0));
            hex.owner = owner;
            hex.target = target;
            level.addFreshEntity(hex);
            return false;
        }

        return true;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            //can survive
            if (!canAlive()) {
                death(false);
                unLockTarget();
                return;
            } else if (tickCount > LIFE_TIME) {//This must be greater than the last tick, otherwise the client cannot execute to the end.
                float amount = target.getMaxHealth() * 0.3f;

                target.calamity$HurtNoInvulnerable(new CalamityDamageSource("player")
                    .setOwner(owner).setNoDecay(amount).setMagic(), amount);
                death(false);

                //If the entity dies normally and the owner is still using the spell book, a new entity can be created
                DelayRunnable.addRunTask(15, () -> {
                    if (reSpawn()) owner.stopUsingItem();
                });
                return;
            }

            if (tickCount % 10 == 0) {
                float amount = (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (expand.calamity$ConsumeMana(Math.min(50f, amount * 3))) {
                    amount += target.getMaxHealth() * 0.03f;
                    target.calamity$HurtNoInvulnerable(new CalamityDamageSource("player")
                        .setOwner(owner).setNoDecay(amount).setMagic(), amount);
                    if (target.isDeadOrDying()) death(reSpawn());
                    else setPos(target.position().add(0, yOffset, 0));
                } else {
                    death(true);
                }
            }
        } else if (tickCount > 166) {
            alpha = (LIFE_TIME - tickCount) / 40f;

            if (tickCount == LIFE_TIME) {
                final float[] f = new float[]{random.nextFloat(), 1};
                explosionEffect(f[0]);

                DelayRunnable.conditionsLoop(() -> {
                    f[0] += (f[1] * 1.0472f);
                    //synchronized (this)
                    explosionEffect(f[0]);

                    f[1]++;
                    return f[1] == 6;
                }, 2);
            }
        }
    }

    private boolean canAlive() {
        return owner != null && owner.isAlive() && owner.isUsingItem()
            && owner.getUseItem().is(CalamityItems.ETERNITY.get());
    }

    private boolean chooseNewTarget() {
        LivingEntity newTarget = CalamityHelp.getClosestTarget(owner, 16, owner.position());
        if (newTarget != null) {
            target = newTarget;
            CalamityHelp.setCalamityFlag(target, 3, true);
            yOffset = target.getBbHeight() / 2;
            return true;
        }

        return false;
    }

    private void death(boolean shouldStop) {
        if (shouldStop) {
            owner.stopUsingItem();
            unLockTarget();
        }

        discard();
    }

    private void unLockTarget() {
        if (target != null && target.isAlive())
            CalamityHelp.setCalamityFlag(target, 3, false);
    }

    @OnlyIn(Dist.CLIENT)
    public final void explosionEffect(float radians) {
        Vector2d randomCirclePointVector = Vector2d.NUNIT_Y.rotatedBy(radians, Vector2d.ZERO, false);

        // pointsPerStarStrip is basically how many times dust should be drawn to make half of a star point.
        // The amount of dust from the explosion = pointsPerStarStrip * starPoints * 2.
        int pointsPerStarStrip = 40;
        int starPoints = 9;
        //Prevents random source thread access issues
        ThreadLocalRandom localRandom = ThreadLocalRandom.current();

        float minStarOutwardness = localRandom.nextFloat(0.6f, 1f);
        float maxStarOutwardness = localRandom.nextFloat(1.4f, 2.4f);
        Vector2d randomCirclePointLerped = new Vector2d(0, 0);

        for (float i = 0; i < starPoints; i++) {
            for (int rotationDirection = -1; rotationDirection <= 1; rotationDirection += 2) {
                Vector2d randomCirclePointRotated = randomCirclePointVector.rotatedBy(
                    rotationDirection * Mth.TWO_PI / (starPoints * 2), Vector2d.ZERO, false);

                for (float k = 0f; k < pointsPerStarStrip; k++) {
                    float v = k / pointsPerStarStrip;
                    randomCirclePointLerped.set(Mth.lerp(v, randomCirclePointVector.x, randomCirclePointRotated.x),
                        Mth.lerp(v, randomCirclePointVector.y, randomCirclePointRotated.y));

                    float outwardness = Mth.lerp(minStarOutwardness, maxStarOutwardness, k / pointsPerStarStrip) * 2f;

                    level.addParticle(ParticleRegister.ETERNITY_DUST.get(), getX(), getY(), getZ(),
                        randomCirclePointLerped.x * outwardness, randomCirclePointLerped.y * outwardness, 0);
                }
            }

            randomCirclePointVector.rotatedBy(Mth.TWO_PI / starPoints, Vector2d.ZERO, true);
        }

        level.playLocalSound(getX(), getY(), getZ(), CalamitySounds.LARGE_WEAPON_FIRE.get(),
            SoundSource.AMBIENT, 2f, 1f, false);
    }

    @OnlyIn(Dist.CLIENT)
    public void determineLemniscatePosition(float partialTick) {
        float scale = 2f / (3f - (float) Math.cos(2 * lemniscateAngle));
        float outwardMultiplier = Mth.lerp(RenderUtil.clampLerp(0, maxFrame, currentFrame, true), 0.04f, 6f);
        lemniscateOffset.set((float) Math.cos(lemniscateAngle), (float) Math.sin(2f * lemniscateAngle) / 2f);
        lemniscateOffset.mul(scale);

        double x = Mth.lerp(partialTick, 0d, getX() - xo);
        double y = Mth.lerp(partialTick, 0d, getY() - yo);
        double z = Mth.lerp(partialTick, 0d, getZ() - zo);
        pos.set(x + lemniscateOffset.x * outwardMultiplier, y - lemniscateOffset.y * outwardMultiplier, z);
    }

    @OnlyIn(Dist.CLIENT)
    private void render(PoseStack pose, MultiBufferSource buffer, float partialTick) {
        currentFrame++;
        for (int c = 0; c < 2; c++) {
            for (int i = 0; i < framerateLimit; i++) {
                lemniscateAngle += ANGLE;
                determineLemniscatePosition(partialTick);
            }

            if ((extraUpdate += partialLimit) > 1f) {
                extraUpdate -= 1f;
                lemniscateAngle += ANGLE;
                determineLemniscatePosition(partialTick);
            }
        }

        //pushPos
        oldPos.push(new Vec3(pos.x, pos.y, pos.z));
        PrimitiveRenderer.renderTrail(oldPos, settings.setBufferSource(buffer), 84, pose);
    }


    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<EternityHex> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EternityHex entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            entity.render(pose, buffer, partialTick);
        }

        @Override
        public ResourceLocation getTextureLocation(EternityHex eternityHex) {
            return CalamityCurios.ModResource("textures/entity/eternity_streak.png");
        }
    }
}
