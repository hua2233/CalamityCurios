package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.generators.DamageMapping;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import hua223.calamity.render.primitive.PrimitiveRenderer;
import hua223.calamity.render.primitive.PrimitiveSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

@AutoEntityRegister(sized = {1f, .5f}, trackingRange = 16)
public class EternityHex extends Entity {
    @DamageRequester(key = DamageMapping.MAGIC_PROJECTILE, msg = "eternity",
        style = ChatFormatting.LIGHT_PURPLE, zh_cn = "%s的生命流逝为了尘埃")
    public static DamageSupplier supplier;

    //Approximately 5.15s in Terraria. 310 frames.
    //This is because Minecraft uses Tick instead of maxFrame rate. Balance using a setting of 60 frames for 20tick。
    private static final int LIFE_TIME = 206;
    private LivingEntity target;
    private Player owner;
    private float yOffset;
    private DamageSource source;

    @OnlyIn(Dist.CLIENT)
    private static final float ANGLE = Mth.TWO_PI / 200f;
    @OnlyIn(Dist.CLIENT)
    private final CircleBuffer<Vector2f> oldPos = CircleBuffer.ofFill(64, Vector2f::new);
    @OnlyIn(Dist.CLIENT)
    private float maxFrame;
    @OnlyIn(Dist.CLIENT)
    private int framerateLimit;
    @OnlyIn(Dist.CLIENT)
    private float partialLimit;
    @OnlyIn(Dist.CLIENT)
    private float lemniscateAngle;
    @OnlyIn(Dist.CLIENT)
    private final Vector2f lemniscateOffset = new Vector2f(0, 0);
    @OnlyIn(Dist.CLIENT)
    private int currentFrame;
    @OnlyIn(Dist.CLIENT)
    private float extraUpdate;
    @OnlyIn(Dist.CLIENT)
    private final Vector2f pos = new Vector2f();
    @OnlyIn(Dist.CLIENT)
    private final EternitySettings settings = new EternitySettings();

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

    public static void create(Player player, IDataPackResponse response, Level level, LivingEntity target) {
        if (player.Calamity$Player.data.getMana() > 30) {
            EternityHex hex = CalamityCurios.getEntityType(EternityHex.class).create(level);
            if (hex != null) {
                hex.yOffset = target.getBbHeight() / 2;
                hex.setPos(target.position().add(0, hex.yOffset, 0));
                hex.owner = player;
                hex.target = target;
                hex.source = supplier.get(hex, player);
                hex.updateLock(response, true);
                level.addFreshEntity(hex);
            }
        } else player.stopUsingItem();
    }

    private boolean reSpawn() {
        if (canAlive() && (!target.isDeadOrDying() || chooseNewTarget())) {
            EternityHex hex = CalamityCurios.getEntityType(EternityHex.class).create(level());
            if (hex != null) {
                hex.yOffset = yOffset;
                hex.source = source;
                hex.setPos(target.position().add(0, yOffset, 0));
                hex.owner = owner;
                hex.target = target;
                level().addFreshEntity(hex);
                return false;
            }
        }

        return true;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            //can survive
            if (!canAlive()) {
                death(false);
                unLockTarget();
                return;
            } else if (tickCount > LIFE_TIME) {//This must be greater than the last tick, otherwise the client cannot tryExecute to the end.
                float amount = target.getMaxHealth() * 0.3f;

                target.hurt(source, amount);
                death(false);

                //If the entity dies normally and the owner is still using the spell book, a new entity can be created
                DelayRunnable.addRunTask(15, () -> {
                    if (reSpawn()) {
                        owner.stopUsingItem();
                        unLockTarget();
                    }
                });
                return;
            }

            if (tickCount % 10 == 0) {
                float amount = (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (owner.Calamity$Player.consumeMana(Math.min(50f, amount * 10))) {
                    amount += target.getMaxHealth() * 0.03f;
                    target.hurt(source, amount);
                    if (target.isDeadOrDying()) death(reSpawn());
                    else setPos(target.position().add(0, yOffset, 0));
                } else {
                    death(true);
                }
            }
        } else if (tickCount > 166) {
            settings.alpha = (LIFE_TIME - tickCount) / 40f;

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

    private void updateLock(IDataPackResponse response, boolean effect) {
        CompoundTag tag = response.getPack();
        target.calamity$EternityLock = effect;
        tag.putBoolean("flag", effect);
        tag.putInt("id", target.getId());
        response.sendToAllClient();
    }

    private boolean canAlive() {
        return owner != null && owner.isAlive() && owner.isUsingItem()
            && owner.getUseItem().is(CalamityItems.ETERNITY.get());
    }

    private boolean chooseNewTarget() {
        LivingEntity newTarget = CalamityHelp.getClosestTarget(owner, 16);
        if (newTarget != null) {
            target = newTarget;
            unLockTarget();
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
        if (target != null && target.isAlive()) updateLock(
            (IDataPackResponse) CalamityItems.ETERNITY.get(), false);
    }

    @OnlyIn(Dist.CLIENT)
    public final void explosionEffect(float radians) {
        Vector2f randomCirclePointVector = Vector2f.NUNIT_Y.rotatedBy(radians, Vector2f.ZERO, false);

        // pointsPerStarStrip is basically how many times dust should be drawn to make half of a star point.
        // The amount of dust from the explosion = pointsPerStarStrip * starPoints * 2.
        int pointsPerStarStrip = 40;
        int starPoints = 9;
        //Prevents random source thread access issues
        ThreadLocalRandom localRandom = ThreadLocalRandom.current();

        float minStarOutwardness = localRandom.nextFloat(0.6f, 1f);
        float maxStarOutwardness = localRandom.nextFloat(1.4f, 2.4f);
        Vector2f randomCirclePointLerped = new Vector2f(0, 0);

        for (float i = 0; i < starPoints; i++) {
            for (int rotationDirection = -1; rotationDirection <= 1; rotationDirection += 2) {
                Vector2f randomCirclePointRotated = randomCirclePointVector.rotatedBy(
                    rotationDirection * Mth.TWO_PI / (starPoints * 2), Vector2f.ZERO, false);

                for (float k = 0f; k < pointsPerStarStrip; k++) {
                    float v = k / pointsPerStarStrip;
                    randomCirclePointLerped.set(Mth.lerp(v, randomCirclePointVector.x, randomCirclePointRotated.x),
                        Mth.lerp(v, randomCirclePointVector.y, randomCirclePointRotated.y));

                    float outwardness = Mth.lerp(minStarOutwardness, maxStarOutwardness, k / pointsPerStarStrip) * 2f;

                    level().addParticle(ParticleRegister.ETERNITY_DUST.get(), getX(), getY(), getZ(),
                        randomCirclePointLerped.x * outwardness, randomCirclePointLerped.y * outwardness, 0);
                }
            }

            randomCirclePointVector.rotatedBy(Mth.TWO_PI / starPoints, Vector2f.ZERO, true);
        }

        level().playLocalSound(getX(), getY(), getZ(), CalamitySounds.LARGE_WEAPON_FIRE.get(),
            SoundSource.PLAYERS, 2f, 1f, false);
    }

    @OnlyIn(Dist.CLIENT)
    public void determineLemniscatePosition() {
        float scale = 2f / (3f - (float) Math.cos(2 * lemniscateAngle));
        float outwardMultiplier = Mth.lerp(RenderUtil.clampLerp(0, maxFrame, currentFrame), 0.04f, 6f);
        lemniscateOffset.set((float) Math.cos(lemniscateAngle), (float) Math.sin(2f * lemniscateAngle) / 2f);
        lemniscateOffset.mul(scale);

        pos.set(lemniscateOffset.x * outwardMultiplier, lemniscateOffset.y * outwardMultiplier);
    }

    @OnlyIn(Dist.CLIENT)
    private void render(PoseStack pose, MultiBufferSource buffer) {
        currentFrame++;
        for (int c = 0; c < 2; c++) {
            for (int i = 0; i < framerateLimit; i++) {
                lemniscateAngle += ANGLE;
                determineLemniscatePosition();
            }

            if ((extraUpdate += partialLimit) > 1f) {
                extraUpdate -= 1f;
                lemniscateAngle += ANGLE;
                determineLemniscatePosition();
            }
        }

        oldPos.fillNext().set(pos);
        settings.source = buffer;
        PrimitiveRenderer.renderTrail(oldPos, settings, 84, pose.last().pose());
    }


    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<EternityHex> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EternityHex entity, float entityYaw, float partialTick, PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight) {
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            entity.render(pose, buffer);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull EternityHex eternityHex) {
            return CalamityCurios.ModResource("textures/entity/eternity_streak.png");
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class EternitySettings extends PrimitiveSettings {
        private float alpha = 1f;
        private MultiBufferSource source;

        private final Vector4i magenta = RenderUtil.fromColorGet(Color.magenta);
        private final Vector4i cyan = RenderUtil.fromColorGet(Color.cyan);
        private final Vector4i finalColor = RenderUtil.black();
        private final Vector4i tailColor = RenderUtil.black();
        private final Vector4i HEAD_COLOR = RenderUtil.interpolateColor(RenderUtil.black(), magenta, 0.1f, null);

        public EternitySettings() {
            super(RenderUtil.Shaders.getLemniscateRenderType(CalamityCurios.ModResource("textures/entity/eternity_streak.png")));
        }

        @Override
        public float vertexWidth(float completionRatio) {
            float widthInterpolant = RenderUtil.clampLerp(0f, 0.12f, completionRatio);
            return RenderUtil.smoothStep(0.02f, 0.25f, widthInterpolant);
        }

        @Override
        public Vector4i vertexColor(float completionRatio) {
            float leftoverTimeScale = (float) Math.sin((double) RenderUtil.getLocalTick() / 15) * 0.5f + 0.5f;
            leftoverTimeScale *= 0.5f;

            RenderUtil.interpolateColor(magenta, cyan, completionRatio * 0.5f + leftoverTimeScale, tailColor);

            float opacity = (float) Math.pow(RenderUtil.clampLerp(1f, 0.61f, completionRatio), 0.4) * alpha;
            float fadeToMagenta = RenderUtil.smoothStep(0f, 1f, (float) Math.pow(completionRatio, 0.6d));
            return RenderUtil.multiplyColor(RenderUtil.interpolateColor(HEAD_COLOR, tailColor, fadeToMagenta, finalColor), opacity, finalColor);
        }

        @Override
        public int getCapacity() {
            return 3;
        }

        @Override
        public float widthCorrectionRatio() {
            return 40;
        }

        @Override
        public VertexConsumer getConsumer() {
            return source.getBuffer(shader);
        }
    }
}
