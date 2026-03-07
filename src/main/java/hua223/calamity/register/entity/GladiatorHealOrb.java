package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GladiatorHealOrb extends Entity {
    private static final int LIFETIME = 400;
    @OnlyIn(Dist.CLIENT)
    private final Vector4f outerColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private final Vector4f innerColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private final Vector4f lightSeaGreen = new Vector4f(32f, 178f, 170f, 255f);
    @OnlyIn(Dist.CLIENT)
    private final Vector4f limeGreen = new Vector4f(50f, 205f, 50f, 255f);
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderUtil.Shaders.getGlowRenderType(
        CalamityCurios.ModResource("textures/entity/small_creyscale_circle.png"));
    private Player followingPlayer;
    @OnlyIn(Dist.CLIENT)
    private float outerScale = 1f;
    @OnlyIn(Dist.CLIENT)
    private float innerScale = 1f;
    @OnlyIn(Dist.CLIENT)
    private final double yAttenuation;

    public GladiatorHealOrb(EntityType<?> entityType, Level level) {
        super(entityType, level);
        double yS = random.nextDouble() * 0.6d;
        yAttenuation = yS / 10;
        setDeltaMovement((2 * random.nextDouble() - 1) * 0.3d, yS, (2 * random.nextDouble() - 1) * 0.3d);

        if (level.isClientSide) changeColor();
    }

    public static void create(LivingEntity target) {
        GladiatorHealOrb orb = CalamityEntity.HEAL_ORB.get().create(target.level);
        orb.setPos(target.getEyePosition());

        target.level.addFreshEntity(orb);
    }

    @Override
    public void tick() {
        if (level.isClientSide) changeColor();

        if (!this.level.noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / (double) 2.0F, this.getZ());
        }

        if (this.tickCount % 20 == 1 && (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0)) {
            this.followingPlayer = this.level.getNearestPlayer(this, 8.0);
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (followingPlayer != null) {
            Vec3 vec3 = new Vec3(followingPlayer.getX() - getX(), followingPlayer.getY() +
                followingPlayer.getEyeHeight() / 2.0 - getY(), followingPlayer.getZ() - getZ());

            double d0 = vec3.lengthSqr();
            if (d0 < 1 && !level.isClientSide) {
                discard();
                followingPlayer.heal(4);
                if (!followingPlayer.hasEffect(MobEffects.REGENERATION))
                    followingPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
            } else if (d0 < 64.0) {
                setDeltaMovement(getDeltaMovement().add(vec3.normalize().scale(0.4)));
            }
        }

        Vec3 movement = getDeltaMovement();
        move(MoverType.SELF, movement);
        float f = 0.98F;
        if (onGround) {
            BlockPos pos = new BlockPos(getX(), getY() - 1.0, getZ());
            f = level.getBlockState(pos).getFriction(level, pos, this) * 0.98F;
        }

        setDeltaMovement(movement.multiply(f, 0.98, f));

        if (onGround) setDeltaMovement(getDeltaMovement().multiply(1.0, -0.9, 1.0));
        else if (followingPlayer == null)
            setDeltaMovement(movement.x * 0.9, movement.y - yAttenuation, movement.z * 0.9);

        if (tickCount > LIFETIME) discard();
    }

    @OnlyIn(Dist.CLIENT)
    private void changeColor() {
        int timeLeft = LIFETIME - tickCount;
        float colorInterpolation = (float) Math.cos(timeLeft / 11f + RenderUtil.getLocalTick() / 7f / Math.PI) * 0.5f + 0.5f;
        RenderUtil.interpolateColor(lightSeaGreen, limeGreen, colorInterpolation, outerColor).mul(0.4f);
        outerColor.setW(255f);

        innerColor.set(outerColor.x() * 0.5f, outerColor.y() * 0.5f, outerColor.z() * 0.5f, 123f);

        outerScale = 0.3f + 0.15f * (float) Math.cos(RenderUtil.getLocalTick() % 20f * Mth.TWO_PI);

        //Shrinks to nothing when projectile is nearing death
        if (timeLeft <= 20) outerScale *= timeLeft / 20f;

        innerScale = outerScale * 0.7f;
        RenderUtil.multiplyColor(outerColor, outerScale, outerColor);
        RenderUtil.multiplyColor(innerColor, outerScale, innerColor);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
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
    public static class Render extends EntityRenderer<GladiatorHealOrb> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        private static void buildVertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f normal, float scale, float z, Vector4f color) {
            int r = (int) color.x();
            int g = (int) color.y();
            int b = (int) color.z();
            int a = (int) color.w();
            consumer.vertex(matrix4f, -scale, scale, z)
                .color(r, g, b, a)
                .uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, scale, scale, z)
                .color(r, g, b, a)
                .uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, scale, -scale, z)
                .color(r, g, b, a)
                .uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, -scale, -scale, z)
                .color(r, g, b, a)
                .uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0f, 1f, 0f)
                .endVertex();
        }

        @Override
        public void render(GladiatorHealOrb entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            Matrix4f matrix4f = pose.last().pose();
            Matrix3f normal = pose.last().normal();
            buildVertex(consumer, matrix4f, normal, entity.innerScale, 0f, entity.innerColor);
            buildVertex(consumer, matrix4f, normal, entity.innerScale, 0.001f, entity.innerColor);
            buildVertex(consumer, matrix4f, normal, entity.innerScale, 0.002f, entity.innerColor);
            buildVertex(consumer, matrix4f, normal, entity.outerScale, 0.003f, entity.outerColor);
            buildVertex(consumer, matrix4f, normal, entity.outerScale, 0.004f, entity.outerColor);
            buildVertex(consumer, matrix4f, normal, entity.outerScale, 0.005f, entity.outerColor);
        }

        @Override
        public ResourceLocation getTextureLocation(GladiatorHealOrb gladiatorHealOrb) {
            return CalamityCurios.ModResource("textures/entity/small_creyscale_circle.png");
        }
    }
}
