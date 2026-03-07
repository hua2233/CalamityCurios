package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import hua223.calamity.util.CalamityCelestialBodyShader;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BlackHolePet extends Entity implements OwnableEntity {
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderUtil.Shaders.getBlackHole();
    @OnlyIn(Dist.CLIENT)
    private float rotation;
    @OnlyIn(Dist.CLIENT)
    private float zRotation;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;

    private Player owner;

    public BlackHolePet(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            if (this.lSteps > 0) {
                double x = (this.lx - this.getX()) / (double) this.lSteps;
                rotation = RenderUtil.angleLerp((float) x, 0.08f, 0.3f);
                double d5 = this.getX() + x;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double z = ((this.lz - this.getZ()) / (double) this.lSteps);
                double d7 = this.getZ() + z;
                --this.lSteps;
                this.setPos(d5, d6, d7);
                zRotation = (float) (z * -0.033f + 1f);
            } else {
                zRotation = 1f;
                rotation = 0f;
            }
        } else if (owner == null || owner.isDeadOrDying()) discard();
        else {
            Vec3[] axis = CalamityHelp.makeBasisFromDirection(owner.getLookAngle());
            setPos(owner.getEyePosition().add(0, 0.24, 0).add(axis[1].scale(1.2)));
        }
    }

    public static UUID create(Player player) {
        BlackHolePet hole = CalamityEntity.BLACK_HOLE.get().create(player.level);
        if (hole != null) {
            hole.owner = player;
            Vec3[] axis = CalamityHelp.makeBasisFromDirection(player.getLookAngle());
            hole.setPos(player.getEyePosition().add(0, 0.24, 0).add(axis[1].scale(1.2)));
            player.getLevel().addFreshEntity(hole);
            return hole.uuid;
        }

        return null;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {}

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
        return new ClientboundAddEntityPacket(this, owner.getId());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        owner = (Player) level.getEntity(packet.getData());
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return owner == null ? null : owner.getUUID();
    }

    @Override
    public @Nullable Entity getOwner() {
        return owner;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<BlackHolePet> {

        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(BlackHolePet entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            VertexConsumer consumer = buffer.getBuffer(entity.type);

            CalamityCelestialBodyShader.setHoleUniform(0.3f, Vector3f.ZERO, 1f,
                new Vector3f(245, 105, 61), 0.32f, new Vector3f(entity.zRotation, 0f, entity.rotation)
                , new Vector3f(1f, 0.33f, 1f), 0.7f, 0.7f, 0.45f);
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Vector3f.ZP.rotation(entity.rotation));

            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            consumer.vertex(matrix4f, -0.75f, -0.75f, 0)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, -0.75f, 0.75f, 0)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 0.75f, 0.75f, 0)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 0.75f, -0.75f, 0)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();
        }

        @Override
        public ResourceLocation getTextureLocation(BlackHolePet blackHolePet) {
            return CalamityCelestialBodyShader.BASE_TEXTURE;
        }
    }
}
