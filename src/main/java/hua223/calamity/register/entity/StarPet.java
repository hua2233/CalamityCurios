package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import hua223.calamity.util.CalamityCelestialBodyShader;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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

public class StarPet extends Entity implements OwnableEntity {
    @OnlyIn(Dist.CLIENT)
    private final RenderType circleSmall = RenderUtil.Shaders.getCircleSmall();
//    @OnlyIn(Dist.CLIENT)
//    private final RenderType radialShine = RenderUtil.Shaders.getRadialShineRenderType();
    @OnlyIn(Dist.CLIENT)
    private final RenderType sun = RenderUtil.Shaders.getSunRenderType();
    @OnlyIn(Dist.CLIENT)
    private final Vector3f[] uniforms = new Vector3f[] {
        new Vector3f(255f, 255f, 255f),
        new Vector3f(204f, 92f, 25f),
        new Vector3f(181f, 0f, 0f)
    };

    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;

    private Player owner;

    public StarPet(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            if (this.lSteps > 0) {
                double d5 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                --this.lSteps;
                this.setPos(d5, d6, d7);
            }
        } else if (owner == null || owner.isDeadOrDying()) discard();
        else {
            Vec3[] axis = CalamityHelp.makeBasisFromDirection(owner.getLookAngle());
            setPos(owner.getEyePosition().add(0, 0.24, 0).add(axis[1].scale(1.2)));
        }
    }

    public static UUID create(Player player) {
        StarPet star = CalamityEntity.SUN.get().create(player.level);
        if (star != null) {
            star.owner = player;
            Vec3[] axis = CalamityHelp.makeBasisFromDirection(player.getLookAngle());
            star.setPos(player.getEyePosition().add(0, 0.24, 0).add(axis[1].scale(1.2)));
            player.getLevel().addFreshEntity(star);
            return star.uuid;
        }

        return null;
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
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {}

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
    public static class Renderer extends EntityRenderer<StarPet> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(StarPet entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            Matrix4f matrix4f = poseStack.last().pose();

            VertexConsumer circleSmall = buffer.getBuffer(entity.circleSmall);
            buildVertex(circleSmall, matrix4f,  0.003f,1.5f,  179, 179, 0, 0);
            buildVertex(circleSmall, matrix4f, 0.002f, 1.82f,  200, 0, 0, 0);

            //FIXME: unresolved background confusion issue
//            buildVertex(buffer.getBuffer(entity.radialShine), matrix4f, 0.001f, 2.8f, 60, 51, 27, 61);
            CalamityCelestialBodyShader.setSunUniform(0.05f, entity.uniforms[0],
                entity.uniforms[1], entity.uniforms[2], RenderUtil.getLocalTick() + partialTick);
            buildVertex(buffer.getBuffer(entity.sun), matrix4f, 0f, 0.85f, 255, 255, 255, 255);
        }

        public static void buildVertex(VertexConsumer consumer, Matrix4f matrix4f, float offset, float size, int r, int g, int b, int a) {
            consumer.vertex(matrix4f, -size, -size, offset)
                .color(r, g, b, a)
                .uv(1, 1)
                .endVertex();

            consumer.vertex(matrix4f, -size, size, offset)
                .color(r, g, b, a)
                .uv(1, 0)
                .endVertex();

            consumer.vertex(matrix4f, size, size, offset)
                .color(r, g, b, a)
                .uv(0, 0)
                .endVertex();

            consumer.vertex(matrix4f, size, -size, offset)
                .color(r, g, b, a)
                .uv(0, 1)
                .endVertex();
        }

        @Override
        public ResourceLocation getTextureLocation(StarPet starPet) {
            return CalamityCelestialBodyShader.DENDRITIC_NOISE;
        }
    }
}
