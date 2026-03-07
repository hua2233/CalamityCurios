package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CalamityDamageSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NebulaNova extends Projectile {
    public double centerY;
    public LivingEntity owner;
    @OnlyIn(Dist.CLIENT)
    private final float[] frame = new float[]{0f, 0.5f, 0f, 0.1428f};
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(Render.TEXTURE);
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    private float scale = 0.2f;
    private boolean upward;

    public NebulaNova(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(scale, scale);
    }

    @Override
    public void tick() {
        if (tickCount == 16) {
            scale += 0.3f;
            refreshDimensions();
        } else if (tickCount == 22) {
            scale += 0.6f;
            refreshDimensions();
        } else if (tickCount == 27) {
            if (level.isClientSide) {
                level.playLocalSound(getX(), getY(), getZ(), CalamitySounds.NEBULA_EXPLODE.get(), SoundSource.AMBIENT, 1f, 1f, true);
            } else {
                List<Entity> entities = level.getEntities(this, this.getBoundingBox().inflate(2),
                    entity -> entity.isPickable() && entity.isAlive() && entity != owner && entity instanceof LivingEntity);

                if (!entities.isEmpty()) {
                    for (Entity entity : entities)
                        entity.hurt(new CalamityDamageSource("player").setOwnerAndIndirect(owner, this)
                            .setMagic().setProjectile(), 9f);
                }
            }
        }

        if (level.isClientSide) {
            if (lSteps > 0) {
                double d5 = getX() + (lx - getX()) / (double) lSteps;
                double d6 = getY() + (ly - getY()) / (double) lSteps;
                double d7 = getZ() + (lz - getZ()) / (double) lSteps;
                --lSteps;
                setPos(d5, d6, d7);
            }

            //update animate
            if (tickCount < 27 && tickCount % 2 == 0) {
                if (tickCount == 14) {
                    frame[0] = 0.5f;
                    frame[1] = 1f;
                    frame[2] = 0f;
                    frame[3] = 0.1428f;
                } else {
                    frame[2] += 0.1428f;
                    frame[3] += 0.1428f;
                }
            }
        } else {
            if (tickCount > 30) {
                discard();
            }

            Vec3 move = getDeltaMovement();
            Vec3 pos = position();
            double y = centerY;
            if (tickCount % 5 == 0) {
                upward = !upward;
                y += (upward ? 0.2 : -0.2);
            }

            setPos(pos.x + move.x, y, pos.z + move.z);
            setDeltaMovement(move.scale(0.98));
        }
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
    protected void defineSynchedData() {
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<NebulaNova> {
        protected static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/entity/nebula_nova.png");

        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(NebulaNova entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            Matrix4f matrix4f = pose.last().pose();
            float[] frame = entity.frame;

            consumer.vertex(matrix4f, -entity.scale, entity.scale, 0)
                .color(255, 255, 255, 255)
                .uv(frame[0], frame[2])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, entity.scale, entity.scale, 0)
                .color(255, 255, 255, 255)
                .uv(frame[1], frame[2])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, entity.scale, -entity.scale, 0)
                .color(255, 255, 255, 255)
                .uv(frame[1], frame[3])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, -entity.scale, -entity.scale, 0)
                .color(255, 255, 255, 255)
                .uv(frame[0], frame[3])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();
        }

        @Override
        public ResourceLocation getTextureLocation(NebulaNova nebulaNova) {
            return TEXTURE;
        }
    }
}
