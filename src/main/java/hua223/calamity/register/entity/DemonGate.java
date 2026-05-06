package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.UUID;

public class DemonGate extends Entity {
    private float health;
    private UUID player;

    @OnlyIn(Dist.CLIENT)
    private int alpha = 255;
    @OnlyIn(Dist.CLIENT)
    private float rotate = 6f;
    @OnlyIn(Dist.CLIENT)
    private final RenderType type =
        RenderType.entityTranslucent(CalamityCurios.ModResource("textures/entity/demon_gate.png"));

    public DemonGate(EntityType<?> entityType, Level level) {
        super(entityType, level);

    }

    @SuppressWarnings("ConstantConditions")
    public static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        DemonGate gate = CalamityEntity.DEMON_GATE.get().create(level);
        gate.noPhysics = true;
        gate.setNoGravity(true);
        gate.setPos(player.position().add(0, 0.05, 0));
        gate.setYRot(player.getYRot());
        gate.player = player.getUUID();
        gate.health = player.getMaxHealth() * 3f;
        level.addFreshEntity(gate);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean canAttack = super.hurt(source, amount);
        if (canAttack) health -= amount;
        return canAttack;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            rotate += 6;
            if (tickCount > 200) alpha = Mth.lerpInt((300 - tickCount) / 100f, 0, 255);
        } else if (tickCount == 200) {
            SummonedAncientKnight.summonedFromDemonGate(this,
                health > 0 ? null : (ServerPlayer) ((ServerLevel) level()).getEntity(player));
        } else if (tickCount > 300) discard();
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 pos) {}

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    public static class Renderer extends EntityRenderer<DemonGate> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(@NotNull DemonGate entity, float entityYaw, float partialTick,
                           @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            poseStack.mulPose(Axis.YP.rotationDegrees(RenderUtil.rotLerpRadians(partialTick, entity.rotate - 6, entity.rotate)));
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();

            float radius = 1.65F + 0.185F * 3;

            consumer.vertex(matrix4f, -radius, 0, radius)
                .color(255, 255, 255, entity.alpha)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, radius, 0, radius)
                .color(255, 255, 255, entity.alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, radius, 0f, -radius)
                .color(255, 255, 255, entity.alpha)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();

            consumer.vertex(matrix4f, -radius, 0f, -radius)
                .color(255, 255, 255, entity.alpha)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(matrix3f, 0f, 1f, 0f)
                .endVertex();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull DemonGate gate) {
            return CalamityCurios.ModResource("textures/entity/demon_gate.png");
        }
    }
}
