package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.AutoEntityRegister;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static hua223.calamity.generators.DamageMapping.*;
import static net.minecraftforge.event.ForgeEventFactory.onProjectileImpact;

@AutoEntityRegister(sized = {1f, 1.5f}, trackingRange = 16, name = "excelsus")
public class ExcelsusPro extends BaseProjectile {
    @DamageRequester(key = "excelsus", tags = {IS_PROJ, BYPASSES_SHIELD,
        BYPASSES_COOLDOWN}, style = ChatFormatting.LIGHT_PURPLE, zh_cn = "%s被切成了碎块")
    public static DamageSupplier supplier;


    private byte penetrationNumber;
    private int id;

    @OnlyIn(Dist.CLIENT)
    //why can't sync yRot of entity? something reset it...
    private boolean directionLock;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private double lxd;
    @OnlyIn(Dist.CLIENT)
    private double lyd;
    @OnlyIn(Dist.CLIENT)
    private double lzd;
    @OnlyIn(Dist.CLIENT)
    private short r = 255;
    @OnlyIn(Dist.CLIENT)
    private short g = 255;
    @OnlyIn(Dist.CLIENT)
    private short b = 255;
    @OnlyIn(Dist.CLIENT)
    private short a = 255;
    @OnlyIn(Dist.CLIENT)
    private short glowA = 100;
    @OnlyIn(Dist.CLIENT)
    private RenderType base;
    @OnlyIn(Dist.CLIENT)
    private RenderType glow;

    public ExcelsusPro(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        lifeTime = 100;
        setDamage(14f);
        setNoGravity(true);
    }

    public static void create(Player player, Level level) {
        Vec3 angle = player.getLookAngle().normalize();
        float yRot = -player.getYRot() - 90;
        Vec3 speed = player.getDeltaMovement();
        Vec3 acceleration = new Vec3(speed.x, player.onGround() ? 0d : speed.y, speed.z);
        Vec3 spawn = player.getEyePosition().add(angle);

        for (int i = 0; i < 3; i++) {
            ExcelsusPro projectile = CalamityCurios.getEntityType(ExcelsusPro.class).create(level);

            if (projectile != null) {
                projectile.id = i;
                projectile.setOwner(player);
                projectile.setYRot(yRot);
                projectile.setPos(spawn);
                projectile.setDeltaMovement(angle.scale(1.5f + level.random.nextFloat() * 0.5f).add(acceleration));
                level.addFreshEntity(projectile);
            }
        }
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            if (lSteps > 0) {
                double d5 = getX() + (lx - getX()) / (double) lSteps;
                double d6 = getY() + (ly - getY()) / (double) lSteps;
                double d7 = getZ() + (lz - getZ()) / (double) lSteps;
                --lSteps;
                setPos(d5, d6, d7);
            }
            if (tickCount > 70) {
                r = (short) ((lifeTime - tickCount) * 8.5);
                g = r;
                b = g;
                a = (short) (100f * (b / 255f));
                glowA = a;
            }
        } else {
            if (tickCount >= lifeTime) {
                discard();
                return;
            }

            Vec3 vec3 = getDeltaMovement();
            setXRot(xRotO - (float) (vec3.length() * 10f));
            move(MoverType.SELF, vec3);
            if (tickCount < 4) setDeltaMovement(vec3.scale(1.02));
            else setDeltaMovement(vec3.scale(0.96));

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

            if (hitResult.getType() != HitResult.Type.MISS && !onProjectileImpact(this, hitResult)) {
                onHit(hitResult);
            }
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        Vec3 move = getDeltaMovement();
        if (move.length() < 0.7d) discard();

        setDeltaMovement(CalamityHelp.calculateReflection(random, move, result.getLocation().subtract(position()).normalize()));

        BlockPos pos = result.getBlockPos();
        float hardness = level().getBlockState(pos).getDestroySpeed(level(), pos);
        if (hardness < 2 && hardness > -1) {
            level().destroyBlock(pos, true);
        } else {
            level().playSound(null, this, SoundEvents.ANVIL_LAND, SoundSource.AMBIENT, 1f, 1f);
            ((ServerLevel) level()).sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY(), getZ(),
                random.nextInt(6, 11), 0.3d, 0.2d, 0.3d, 0.2d);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yr, float xr, int steps, boolean b) {
        lx = x;
        ly = y;
        lz = z;
        lSteps = steps;
        xRotO = getXRot();
        setXRot(xr);
        setDeltaMovement(lxd, lyd, lzd);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double lerpX, double lerpY, double lerpZ) {
        lxd = lerpX;
        lyd = lerpY;
        lzd = lerpZ;
        setDeltaMovement(lxd, lyd, lzd);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity.isAlive() && entity.isPickable() && entity instanceof LivingEntity livingEntity) {
            attack(livingEntity);
            penetrationNumber++;
            if (penetrationNumber == 3) discard();
            else setDeltaMovement(getDeltaMovement().scale(0.95));
        }
    }

    @Override
    public void setYRot(float pYRot) {
        if (!directionLock) super.setYRot(pYRot);
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(supplier.get(getOwner()), damage);
        target.addEffect(new MobEffectInstance(CalamityEffects.GOD_SLAYER_INFERNO.get(), 60, 0));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, id);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        id = packet.getData();
        base = RenderType.entityTranslucent(getTexture(false));
        glow = RenderType.entityTranslucent(getTexture(true));
        directionLock = true;
    }

    @OnlyIn(Dist.CLIENT)
    protected ResourceLocation getTexture(boolean isGlow) {
        StringBuilder builder = new StringBuilder("textures/entity/excelsus_");

        builder.append(switch (id) {
            case 0 -> "blue";
            case 1 -> "main";
            case 2 -> "pink";
            default -> throw new IllegalStateException("Unexpected value: " + id);
        });

        return CalamityCurios.ModResource(builder.append(isGlow ? "_glow.png" : ".png").toString());
    }


    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<ExcelsusPro> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(ExcelsusPro entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.translate(0, 0.8, 0);
            pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            //There will be a little problem with normals, but too fast should not be able to see clearly ~_~!
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot())));
            VertexConsumer base = buffer.getBuffer(entity.base);
            Matrix4f matrix4f = pose.last().pose();
            Matrix3f normal = pose.last().normal();
            base.vertex(matrix4f, 0.5f, 0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.a).uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();

            base.vertex(matrix4f, -0.5f, 0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.a).uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();

            base.vertex(matrix4f, -0.5f, -0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.a).uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();

            base.vertex(matrix4f, 0.5f, -0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.a).uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();

            //render Glow
            VertexConsumer glow = buffer.getBuffer(entity.glow);
            glow.vertex(matrix4f, 0.5f, 0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.glowA).uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();

            glow.vertex(matrix4f, -0.5f, 0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.glowA).uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();

            glow.vertex(matrix4f, -0.5f, -0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.glowA).uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();

            glow.vertex(matrix4f, 0.5f, -0.75f, 0.0f).color(entity.r, entity.g, entity.b, entity.glowA).uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
            pose.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(ExcelsusPro excelsus) {
            return excelsus.getTexture(false);
        }
    }
}
