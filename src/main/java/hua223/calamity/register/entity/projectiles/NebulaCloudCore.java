package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.entity.AutoEntityRegister;
import hua223.calamity.register.sounds.CalamitySounds;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

//I felt a little uncomfortable in my body and my head was dizzy...
@AutoEntityRegister(sized = {1f, 1f}, trackingRange = 16)
public class NebulaCloudCore extends Projectile {
    @OnlyIn(Dist.CLIENT)
    private int alpha = 0;
    @OnlyIn(Dist.CLIENT)
    private int alphaOld = 0;
    @OnlyIn(Dist.CLIENT)
    private float rotation;
    @OnlyIn(Dist.CLIENT)
    private float rotationOld;
    @OnlyIn(Dist.CLIENT)
    private float height;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private final Vector4i coreColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private final Vector4i cloudColor = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private final Vector4i black = RenderUtil.black();
    @OnlyIn(Dist.CLIENT)
    private float scaleOld = 0.4f;
    @OnlyIn(Dist.CLIENT)
    private final RenderType nebula = RenderType.entityTranslucent(CalamityCurios.ModResource("textures/entity/nebula_cloud.png"));
    @OnlyIn(Dist.CLIENT)
    private final RenderType core = RenderType.entityTranslucent(CalamityCurios.ModResource("textures/entity/nebula_cloud_core.png"));
    @OnlyIn(Dist.CLIENT)
    private float frameV1;
    @OnlyIn(Dist.CLIENT)
    private float frameV2;

    private LivingEntity target;
    private byte hurtCount;
    private float scale = 0.4f;
    private DamageSource source;
    private float damage;

    public NebulaCloudCore(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("ConstantConditions")
    public static void create(LivingEntity player) {
        Level level = player.level();
        LivingEntity target = CalamityHelp.getClosestTarget(player, 14);
        if (target != null) {
            Vec3 look = player.getLookAngle().normalize();
            NebulaCloudCore core = CalamityCurios.getEntityType(NebulaCloudCore.class).create(level);
            core.setDeltaMovement(look.scale(0.8 + level.random.nextDouble()));
            core.setPos(player.getEyePosition().add(look.scale(0.5)));
            core.target = target;
            core.setOwner(player);
            core.height = target.getBbHeight() * 0.7f;
            AbstractSpell spell = RegisterList.NEBULOUS.get();
            core.damage = (float) player.getAttribute(Attributes.ATTACK_DAMAGE).getValue() + spell.getSpellPower(1, player);
            core.source = spell.getDamageSource(core, player);
            level.addFreshEntity(core);
            CalamitySounds.NEBULA.playSound(player);
        }
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(scale, scale);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            //This is the max speed
            frameV1 = tickCount % 4 * 0.25f;
            frameV2 = frameV1 + 0.25f;
            rotationOld = rotation;
            rotation -= (float) (Math.PI / 10f);
            spawnParticle();

            alphaOld = alpha;
            if (alpha != 255) {
                alpha += 15;
                if (alpha >= 255) alpha = 255;
            }

            if (lSteps > 0) {
                double d5 = getX() + (lx - getX()) / (double) lSteps;
                double d6 = getY() + (ly - getY()) / (double) lSteps;
                double d7 = getZ() + (lz - getZ()) / (double) lSteps;
                --lSteps;
                setPos(d5, d6, d7);
            }
        }

        //Scale up, decelerate and rotate.
        if (firstTick) {
            if (tickCount < 9) {
                if (tickCount >= 2) {
                    if (level().isClientSide) {
                        scaleOld = scale;
                        rotation -= (float) Math.PI / 5f;
                    }
                    scale += 0.3f;
                    refreshDimensions();
                }

                if (!level().isClientSide) {
                    Vec3 move = getDeltaMovement();
                    setPos(position().add(move));
                    setDeltaMovement(move.scale(0.9f));
                }
            } else {
                firstTick = false;

                if (level().isClientSide) scaleOld = scale;
                else if ((target != null || findNewTarget()) && distanceTo(target) > 1.5F)
                    setDeltaMovement(target.position().add(0, height, 0).subtract(position()).normalize().scale(0.3));
            }
        } else if (!level().isClientSide) {
            if (!(target != null && target.isAlive() || findNewTarget()) || tickCount >= 300) {
                onKill();
                return;
            }


            Entity player = getOwner();
            boolean trigger = tickCount % 10 == 0;
            if (trigger) for (LivingEntity entity : CalamityHelp.getAttackableEntity(LivingEntity.class, player == null ? this : player, getBoundingBox()))
                entity.hurt(source, damage / 2f);

            Vec3 endPos = target.position().add(0, height, 0);
            if (distanceToSqr(endPos) > 0.5F) setPos(position().add(endPos.subtract(position()).normalize().scale(0.25)));
            else if (trigger) {
                //Is this a bite in a sense?
                target.hurt(source, damage);
                if (++hurtCount >= 6) onKill();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticle() {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        Vec3[] axis = CalamityHelp.makeBasisFromDirection(getDeltaMovement());
        //A little bit behind to prevent it from being blocked by itself
        Vec3 zOffset = axis[2].scale(0.5f);
        Vec3 spawn = position().subtract(zOffset.x, (zOffset.y - scale / 2), zOffset.z);

        for (int i = 0; i < 2; i++) {
            Vec3[] offsetAndSpeed = RenderUtil.sampleRadialPosAndTangentVel(axis, random, Vector2f.NUNIT_Y, Mth.TWO_PI,  1.5f, 0.3, 0.5);
            EndRodParticle particle = (EndRodParticle) engine.makeParticle(ParticleTypes.END_ROD,
                spawn.x + offsetAndSpeed[0].x, spawn.y + offsetAndSpeed[0].y, spawn.z + offsetAndSpeed[0].z,
                offsetAndSpeed[1].x, offsetAndSpeed[1].y, offsetAndSpeed[1].z);

            if (particle != null) {
                particle.setLifetime(12);

                if (firstTick) {
                    particle.scale(0.3f + random.nextFloat() * 0.4f);
                    if (random.nextBoolean()) {
                        particle.setColor(12536242);
                        particle.setFadeColor(5050434);
                    } else {
                        particle.setColor(11215139);
                        particle.setFadeColor(4793374);
                    }
                    engine.add(particle);
                } else if (random.nextBoolean()) {
                    particle.scale(0.4f + random.nextFloat() * 0.6f);
                    particle.setColor(11215139);
                    particle.setFadeColor(4793374);
                    engine.add(particle);

                    Vec3[] p2 = RenderUtil.sampleRadialPosAndTangentVel(axis, random, Vector2f.NUNIT_Y, Mth.TWO_PI,  1.5f, 0.3, 0.5);
                    EndRodParticle p = (EndRodParticle) engine.makeParticle(ParticleTypes.END_ROD,
                        spawn.x + p2[0].x, spawn.y + p2[0].y, spawn.z + p2[0].z,
                        p2[1].x, p2[1].y, p2[1].z);
                    if (p != null) {
                        p.setLifetime(12);
                        p.scale(0.5f + random.nextFloat() * 0.8f);
                        p.setColor(12536242);
                        p.setFadeColor(10106514);
                        engine.add(p);
                    }
                } else {
                    particle.scale(0.4f + random.nextFloat() * 0.6f);
                    particle.setColor(2236231);
                    particle.setFadeColor(197126);
                    engine.add(particle);
                }
            }
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

    private boolean findNewTarget() {
        Entity player = getOwner();
        if (player != null) {
            AABB scope = getBoundingBox().inflate(10);
            target = CalamityHelp.getClosestTarget(player, scope, position());
            if (target == null) CalamityHelp.getClosestTarget(player, scope, player.position());
            if (target != null) return true;
        }

        onKill();
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void onKill() {
        if (isAlive()) {
            int totalProjectiles = random.nextInt(6, 9);
            float radians = Mth.TWO_PI / totalProjectiles;
            float velocity = 0.7f + random.nextFloat() * 0.3f;
            Vec3 pos = position().add(0, scale / 2, 0);
            Vector2f spinningPoint = new Vector2f(0, -velocity);

            for (int k = 0; k < totalProjectiles; k++) {
                Vector2f velocity2 = spinningPoint.rotatedBy(radians * k, Vector2f.ZERO, false).mul(0.2);
                NebulaNova nova = CalamityCurios.getEntityType(NebulaNova.class).create(level());
                nova.setPos(pos.x + random.nextDouble() * 0.6 - 0.3, pos.y, pos.z + random.nextDouble() * 0.6 - 0.3);
                nova.setDeltaMovement(velocity2.x, 0, velocity2.y);
                nova.centerY = pos.y;
                nova.damage = damage * .75f;
                nova.setOwner(getOwner());
                level().addFreshEntity(nova);
            }
        }

        discard();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientRemoval() {
        Vec3 pos = position().add(0, scale / 2, 0);
        Vec3[] axis = CalamityHelp.makeBasisFromDirection(Minecraft.getInstance().player.getLookAngle());
        ParticleEngine engine = Minecraft.getInstance().particleEngine;

        float alpha = 155f / 255f;
        for (int i = 0; i < 6; i++)
            spawnParticle(engine, axis, 2236231, 197126, true, alpha, 0.7f, pos);

        float alpha2 = 55f / 255f;
        for (int i = 0; i < 45; i++) {
            spawnParticle(engine, axis, 13787647, 6628514, true, alpha2, 1.4f, pos);
            spawnParticle(engine, axis, 8388736, 8388736, true, alpha, 0.7f, pos);
            if (i < 15) {
                spawnParticle(engine, axis, 13787647, 6628514, false, 1f, 0.9f, pos);
                spawnParticle(engine, axis, 2236231, 197126, false, 1f, 0.7f, pos);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticle(ParticleEngine engine, Vec3[] axis, int color, int fadeColor, boolean isY, float alpha, float scale, Vec3 spawnPos) {
        Vec3 offset = RenderUtil.sampleRadialPos(axis, random, isY ? Vector2f.NUNIT_Y : Vector2f.UNIT_X,
            Mth.PI, null, random.nextDouble() * 3);

        EndRodParticle particle = (EndRodParticle) engine.makeParticle(ParticleTypes.END_ROD,
            spawnPos.x + offset.x, spawnPos.y + offset.y, spawnPos.z + offset.z, 0, 0, 0);

        if (particle != null) {
            particle.setColor(color);
            particle.setFadeColor(fadeColor);
            particle.setLifetime(12);
            particle.alpha = alpha;
            particle.gravity = 0;
            particle.scale(scale);
            engine.add(particle);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private Vector4i getCoreColor(float partialTick) {
        int a = alphaOld == 255 ? alphaOld : (int) Mth.lerp(partialTick, alphaOld, alpha);

        coreColor.set(a, a, a, a);
        return coreColor;
    }

    @Override
    protected void defineSynchedData() {
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<NebulaCloudCore> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(NebulaCloudCore entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            Vector4i coreColor = entity.getCoreColor(partialTick);
            Vector4i cloudColor = RenderUtil.interpolateColor(coreColor, entity.black, 0.5f, entity.cloudColor);
            cloudColor.w = coreColor.w;

            float rotation = RenderUtil.rotLerpRadians(partialTick, entity.rotationOld, entity.rotation);
            float rotationScale = 0.95f + (float) (Vector2f.toRotationVector2(rotation * 0.75f).y * 0.1f);

            //Render three nebulae, distinguish them in color, size, and orientation and superimpose them on each other
            pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            VertexConsumer nebula = buffer.getBuffer(entity.nebula);
            float s = entity.firstTick ? entity.scale : Mth.lerp(partialTick, entity.scaleOld, entity.scale);

            vertexBuild(pose, nebula, RenderUtil.multiplyColor(cloudColor, rotationScale, cloudColor),
                0.6f + s * 0.6f * rotationScale, -rotation + 0.35f, 0.003f, 0f, 1f, true);

            vertexBuild(pose, nebula, coreColor, s, -rotation, 0.002F, 0f, 1f, true);

            RenderUtil.multiplyColor(coreColor, 0.8f, coreColor);
            vertexBuild(pose, nebula, coreColor, s * 0.9f, rotation * 0.5f, 0F, 0, 1f, false);

            coreColor.w = coreColor.w / 2;
            vertexBuild(pose, buffer.getBuffer(entity.core), coreColor, s, -rotation * 0.7F, 0.001f, entity.frameV1, entity.frameV2, true);
        }

        private void vertexBuild(PoseStack pose, VertexConsumer consumer, Vector4i color, float scale, float rotation, float z,
                                 float v, float v2, boolean flipHorizontally) {
            float u1 = 1f;
            float u2 = 0f;
            float u3 = 0f;
            float u4 = 1f;
            if (flipHorizontally) {
                float f = u1;
                u1 = u3;
                u3 = f;

                f = u2;
                u2 = u4;
                u4 = f;
            }

            pose.pushPose();
            pose.translate(0, 1, 0);
            pose.mulPose(Axis.ZP.rotation(rotation));
            Matrix4f matrix4f = pose.last().pose();

            consumer.vertex(matrix4f, -scale, scale, z)
                .color(color.x, color.y, color.z, color.w)
                .uv(u1, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, scale, scale, z)
                .color(color.x, color.y, color.z, color.w)
                .uv(u2, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, scale, -scale, z)
                .color(color.x, color.y, color.z, color.w)
                .uv(u3, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, -scale, -scale, z)
                .color(color.x, color.y, color.z, color.w)
                .uv(u4, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0f, 1f, 0f)
                .endVertex();

            pose.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull NebulaCloudCore nebulaCloudCore) {
            return CalamityCurios.ModResource("textures/entity/nebula_cloud.png");
        }
    }
}
