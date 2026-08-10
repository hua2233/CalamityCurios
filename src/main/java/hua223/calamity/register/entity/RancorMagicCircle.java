package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@AutoEntityRegister(sized = {0.1f, 0.1f}, trackingRange = 32, name = "rancor")
public class RancorMagicCircle extends Entity {
    @OnlyIn(Dist.CLIENT)
    private final RenderType circle =
        RenderUtil.Shaders.getRancorCircleRenderType(
            CalamityCurios.ModResource("textures/entity/rancor_magic_circle.png"), false, true);
    @OnlyIn(Dist.CLIENT)
    private final RenderType circleGlowMask =
        RenderUtil.Shaders.getRancorCircleRenderType(
            CalamityCurios.ModResource("textures/entity/rancor_magic_circle_glowmask.png"), true, false);
    @OnlyIn(Dist.CLIENT)
    private final RenderType inner =
        RenderUtil.Shaders.getRancorCircleRenderType(
            CalamityCurios.ModResource("textures/entity/rancor_magic_circle_inner.png"), false, false);
    @OnlyIn(Dist.CLIENT)
    private final RenderType innerGlowMask =
        RenderUtil.Shaders.getRancorCircleRenderType(
            CalamityCurios.ModResource("textures/entity/rancor_magic_circle_inner_glowmask.png"), true, true);
    Player owner;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private float roll;
    @OnlyIn(Dist.CLIENT)
    private float scale;
    @OnlyIn(Dist.CLIENT)
    private float lastScale;
    @OnlyIn(Dist.CLIENT)
    private float alpha;

    public RancorMagicCircle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noPhysics = true;
        setNoGravity(true);
    }

    public static void create(Player owner) {
        Level level = owner.level();
        RancorMagicCircle circle = CalamityCurios.getEntityType(RancorMagicCircle.class).create(level);
        if (circle != null) {
            circle.owner = owner;
            circle.setPos(owner.getEyePosition().add(owner.getLookAngle().normalize().multiply(2f, 2f, 2f)));

            level.addFreshEntity(circle);
        }
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            if (roll >= 6.28318f) roll = 0;
            else roll += 0.10472f;
            if (scale >= 1f) lastScale = scale;

            if (tickCount < 10) {
                scale += 0.1f;
                alpha += 0.1f;
            }

            if (this.lSteps > 0) {
                double d5 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                setYRot((float) -Math.toRadians(owner.getYRot()));
                setXRot((float) Math.toRadians(owner.getXRot()));
                --this.lSteps;
                this.setPos(d5, d6, d7);
            }
        } else {
            if (owner == null || !owner.isAlive() || !owner.isUsingItem() || !owner.getUseItem().is(CalamityItems.RANCOR.get())) {
                discard();
            }

            if (tickCount == 90) RancorLaserBeam.create(owner.level(), this);
            moveTo(owner.getEyePosition().add(owner.getLookAngle().multiply(2f, 2f, 2f)));
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
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float pAmount) {
        return false;
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
        return new ClientboundAddEntityPacket(this, owner == null ? -1 : owner.getId());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        owner = (Player) level().getEntity(packet.getData());
        if (owner != null) {
            setYRot((float) -Math.toRadians(owner.getYRot()));
            setXRot((float) Math.toRadians(owner.getXRot()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<RancorMagicCircle> {
        //以原像素大小到3d中的m进行缩放矩阵
        private static final float GLOW_MASK_SCALE = 118f / 114f;
        private static final float INNER_SCALE = 42f / 114f;
        private static final float INNER_GLOW_MASK_SCALE = 38f / 114f;

        public Renderer(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        private static void matrixPreSet(PoseStack stack, RancorMagicCircle entity, float scale, float s, float zOffset, boolean roll) {
            stack.mulPose(Axis.YP.rotation(entity.getYRot()));
            stack.mulPose(Axis.XP.rotation(entity.getXRot()));
            if (roll) stack.mulPose(Axis.ZP.rotation(entity.roll));
            stack.scale(scale, scale, scale);
            stack.translate(0f, 0f, zOffset);
            stack.scale(s, s, s);
        }

        private static void vertexBuild(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, float alpha) {
            consumer.vertex(matrix4f, -2f, 2f, 0f)
                .color(1f, 0f, 0f, alpha)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, 2f, 2f, 0f)
                .color(1f, 0f, 0f, 1f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, 2f, -2f, 0f)
                .color(1f, 0f, 0f, 1f)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, -2f, -2f, 0f)
                .color(1f, 0f, 0f, 1f)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();
        }

        @Override
        public void render(RancorMagicCircle entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            //以其大小决定其渲染排序，从小到大，从前向后。防止Z轴重叠。一般来说，处理其相较大的才写入深度信息，防止它们互相遮挡
            float s = Mth.lerp(entity.lastScale, entity.scale, partialTick);
            poseStack.pushPose();
            matrixPreSet(poseStack, entity, 1f, s, 0.0011f, true);
            vertexBuild(buffer.getBuffer(entity.circle),
                poseStack.last().pose(), poseStack.last().normal(), entity.alpha);
            poseStack.popPose();

            poseStack.pushPose();
            matrixPreSet(poseStack, entity, GLOW_MASK_SCALE, s, 0.0012f, true);
            vertexBuild(buffer.getBuffer(entity.circleGlowMask),
                poseStack.last().pose(), poseStack.last().normal(), entity.alpha);
            poseStack.popPose();

            poseStack.pushPose();
            matrixPreSet(poseStack, entity, INNER_SCALE, s, 0.001f, false);
            vertexBuild(buffer.getBuffer(entity.inner),
                poseStack.last().pose(), poseStack.last().normal(), entity.alpha);
            poseStack.popPose();

            poseStack.pushPose();
            matrixPreSet(poseStack, entity, INNER_GLOW_MASK_SCALE, s, 0f, false);
            vertexBuild(buffer.getBuffer(entity.innerGlowMask),
                poseStack.last().pose(), poseStack.last().normal(), entity.alpha);
            poseStack.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull RancorMagicCircle rancorMagicCircle) {
            return CalamityCurios.ModResource("textures/entity/rancor_magic_circle.png");
        }
    }
}
