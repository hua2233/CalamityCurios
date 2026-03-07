package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CircleBuffer;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.primitive.PrimitiveRenderer;
import hua223.calamity.util.primitive.PrimitiveSettings;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.EntityDamageSource;
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

import java.util.List;

public class RancorLaserBeam extends Entity {
    @OnlyIn(Dist.CLIENT)
    private final Vector4f smoothColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    public final CircleBuffer<Vec3> position = new CircleBuffer<>(24);
    @OnlyIn(Dist.CLIENT)
    private final Vector4f vibrantColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private float laserLength;
    private RancorMagicCircle circle;
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
    private float partialTick;

    @OnlyIn(Dist.CLIENT)
    private final PrimitiveSettings settings = new PrimitiveSettings(
        c -> tickCount < 5 ? scale - Mth.lerp(partialTick, 0.19f, 0f) : scale,

        c -> {
            RenderUtil.interpolateColor(RenderUtil.BLUE, RenderUtil.RED, (float) Math.cos(RenderUtil.getLocalTick() * 0.67f -
                c / laserLength * 29f) * 0.5f + 0.5f, vibrantColor);

            float opacity = (float) (RenderUtil.clampLerp(0.97f, 0.9f, c, true) *
                RenderUtil.clampLerp(0f, Mth.clamp(0.1f / laserLength, 0f, 0.5f), c, true) *
                Math.pow(RenderUtil.clampLerp(0.6f, 1.8f, laserLength, true), 3D));

            RenderUtil.multiplyColor(RenderUtil.interpolateColor(vibrantColor, RenderUtil.WHITE, 0.5f, smoothColor), opacity, smoothColor);
            return RenderUtil.multiplyColor(smoothColor, 2f, smoothColor);
        },
        true, 10, 10, RenderUtil.Shaders.getRancorLaserRenderType(CalamityCurios.ModResource("textures/entity/perlin.png")));

    private EntityDamageSource source;

    public RancorLaserBeam(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noPhysics = true;
        setNoGravity(true);
    }

    public static void create(Level level, RancorMagicCircle circle) {
        RancorLaserBeam beam = CalamityEntity.RANCOR_LASER.get().create(level);
        beam.circle = circle;
        beam.source = new EntityDamageSource("player", circle.owner);
        beam.source.setMagic();
        beam.source.setIsFire();
        beam.setPos(circle.position());
        level.addFreshEntity(beam);
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
        BlockHitResult result = level.clip(new ClipContext(start, end,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));

        if (result.getType() != HitResult.Type.MISS) {
            end = result.getLocation();
            maxDistance = end.subtract(start);
        }

        if (!level.isClientSide) {
            setPos(circle.position());

            List<? extends Entity> entities = level.getEntities(circle.owner, circle.owner.getBoundingBox().
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
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
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
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, circle == null ? -1 : circle.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int id = packet.getData();
        circle = (RancorMagicCircle) level.getEntity(id);
    }

    public static class Render extends EntityRenderer<RancorLaserBeam> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public void render(RancorLaserBeam entity, float pEntityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            entity.partialTick = partialTick;
            PrimitiveRenderer.renderVec3Trail(entity.position, entity.settings.setBufferSource(buffer),
                PrimitiveRenderer.TrailOrientation.SCREEN_ALIGNED_VERTICAL,96, pose);
        }

        @Override
        public ResourceLocation getTextureLocation(RancorLaserBeam rancorLaserBeam) {
            return CalamityCurios.ModResource("textures/entity/perlin.png");
        }
    }
}
