package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.render.primitive.PrimitiveSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;

import java.awt.*;
import java.util.List;

@AutoEntityRegister(sized = {.1f, .1f}, trackingRange = 32, name = "rancor_laser")
public class RancorLaserBeam extends Entity {
    private RancorMagicCircle circle;

    @OnlyIn(Dist.CLIENT)
    public final CircleBuffer<Vec3> position = new CircleBuffer<>(24);
    @OnlyIn(Dist.CLIENT)
    private float laserLength;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private float scale = 0.05f;

    @OnlyIn(Dist.CLIENT)
    private final PrimitiveSettings settings = new RancorSettings();

    private DamageSource source;

    public RancorLaserBeam(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noPhysics = true;
        setNoGravity(true);
    }

    public static void create(Level level, RancorMagicCircle circle) {
        RancorLaserBeam beam = CalamityCurios.getEntityType(RancorLaserBeam.class).create(level);
        if (beam != null) {
            beam.circle = circle;
            beam.source = level.damageSources().inFire();
            beam.setPos(circle.position());
            level.addFreshEntity(beam);
        }
    }

    @Override
    public void tick() {
        if (circle == null || !circle.isAlive()) {
            discard();
            return;
        }

        Vec3 direction = circle.owner.getLookAngle().normalize();
        Vec3 maxDistance = direction.scale(16);
        Vec3 start = circle.owner.getEyePosition();
        Vec3 end = start.add(maxDistance);
        BlockHitResult result = level().clip(new ClipContext(start, end,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));

        if (result.getType() != HitResult.Type.MISS) {
            end = result.getLocation();
            maxDistance = end.subtract(start);
        }

        if (!level().isClientSide) {
            setPos(circle.position());

            List<? extends Entity> entities = level().getEntities(circle.owner, circle.owner.getBoundingBox().
                expandTowards(maxDistance), entity -> entity.isPickable() && entity.isAlive());

            for (Entity entity : entities) {
                if (entity instanceof LivingEntity living && living.getBoundingBox().clip(start, end).isPresent()) {
                    living.hurt(source, living.getMaxHealth() * 0.1f);
                }
            }
        } else {
            if (tickCount < 5) scale += 0.19f;

            if (this.lSteps > 0) {
                double d5 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                --this.lSteps;
                this.setPos(d5, d6, d7);
            }

            for (float i = position.size; i > 1; i--) {
                float scale = i / position.size;
                position.push(maxDistance.scale(scale));
            }

            position.push(Vec3.ZERO);
            laserLength = (float) position.getLast().length();
        }
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
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yr, float xr, int steps, boolean b) {
        this.lx = x;
        this.ly = y;
        this.lz = z;
        this.lSteps = steps;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, circle == null ? -1 : circle.getId());
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int id = packet.getData();
        circle = (RancorMagicCircle) level().getEntity(id);
    }

    public static class Renderer extends EntityRenderer<RancorLaserBeam> {
        public Renderer(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public void render(RancorLaserBeam entity, float pEntityYaw, float partialTick, @NotNull PoseStack pose,
                           @NotNull MultiBufferSource buffer, int packedLight) {
//            PrimitiveRenderer.renderVec3Trail(entity.position, entity.settings.setBufferSource(buffer),
//                PrimitiveRenderer.TrailOrientation.SCREEN_ALIGNED_VERTICAL,96, pose);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull RancorLaserBeam rancorLaserBeam) {
            return CalamityCurios.ModResource("textures/entity/perlin.png");
        }
    }

    @OnlyIn(Dist.CLIENT)
    private class RancorSettings extends PrimitiveSettings {
        private MultiBufferSource source;

        private final Vector4i smoothColor = RenderUtil.black();
        private final Vector4i vibrantColor = RenderUtil.black();
        private final Vector4i blue = RenderUtil.fromColorGet(Color.BLUE);
        private final Vector4i red = RenderUtil.fromColorGet(Color.RED);
        private final Vector4i white = RenderUtil.fromColorGet(Color.WHITE);

        public RancorSettings() {
            super(RenderUtil.Shaders.getRancorLaserRenderType(CalamityCurios.ModResource("textures/entity/perlin.png")));
        }

        @Override
        public float vertexWidth(float completionRatio) {
            return tickCount < 5 ? scale - Mth.lerp(Minecraft.getInstance().getFrameTime(), 0.19f, 0f) : scale;
        }

        @Override
        public Vector4i vertexColor(float completionRatio) {
            RenderUtil.interpolateColor(blue, red, (float) Math.cos(RenderUtil.getLocalTick() * 0.67f -
                completionRatio / laserLength * 29f) * 0.5f + 0.5f, vibrantColor);

            float opacity = (float) (RenderUtil.clampLerp(0.97f, 0.9f, completionRatio) *
                RenderUtil.clampLerp(0f, Mth.clamp(0.1f / laserLength, 0f, 0.5f), completionRatio) *
                Math.pow(RenderUtil.clampLerp(0.6f, 1.8f, laserLength), 3D));

            RenderUtil.multiplyColor(RenderUtil.interpolateColor(vibrantColor, white, 0.5f, smoothColor), opacity, smoothColor);
            return RenderUtil.multiplyColor(smoothColor, 2f, smoothColor);
        }

        @Override
        public int getCapacity() {
            return 10;
        }

        @Override
        public float widthCorrectionRatio() {
            return 10;
        }

        @Override
        public VertexConsumer getConsumer() {
            return source.getBuffer(shader);
        }
    }
}
