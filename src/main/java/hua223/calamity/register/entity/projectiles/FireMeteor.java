package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.entity.AutoEntityRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static hua223.calamity.util.RenderUtil.addVertex;

@AutoEntityRegister(sized = {0.6f, 0.6f}, trackingRange = 8)
public class FireMeteor extends BaseProjectile {
    private LivingEntity target;
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(CalamityCurios.ModResource("textures/entity/fire_meteor.png"));

    public FireMeteor(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public static void of(Level level, LivingEntity target, ServerPlayer player) {
        FireMeteor fireMeteor = CalamityCurios.getEntityType(FireMeteor.class).create(level);
        if (fireMeteor != null) {
            Vec3 pos = new Vec3(
                target.getX() + level.random.nextDouble() * 3,
                target.getY() + level.random.nextInt(6, 12),
                target.getZ() + level.random.nextDouble() * 3
            );
            fireMeteor.target = target;
            fireMeteor.setOwner(player);
            fireMeteor.setPos(pos);
            level.addFreshEntity(fireMeteor);
        }
    }

    @Override
    protected void logic() {
        Vec3 targetPos = target.position();
        Vec3 targetMotion = target.getDeltaMovement().normalize();
        Vec3 predictedPos = targetPos.add(targetMotion.scale(10)); // 提前量预测
        this.setDeltaMovement(predictedPos.subtract(this.position()).scale(0.5));
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(DamageSupplier.MAGIC_PROJECTILE.get(this, getOwner()), 25);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<FireMeteor> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull FireMeteor entity) {
            return CalamityCurios.ModResource("textures/entity/fire_meteor.png");
        }

        @Override
        public void render(FireMeteor entity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource pBuffer, int packedLight) {
            poseStack.pushPose();

            poseStack.translate(0F, 0.3F, 0F);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

            int frameCount = 3;
            int frameTime = 2;
            int frameIndex = (entity.tickCount / frameTime) % frameCount;

            float frameHeight = 1.0f / frameCount;
            float v0 = frameIndex * frameHeight;
            float v1 = v0 + frameHeight;

            VertexConsumer vertexConsumer = pBuffer.getBuffer(entity.type);

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            addVertex(pose, normal, vertexConsumer, -0.5F, -0.5F, 0, 0, v1, packedLight);
            addVertex(pose, normal, vertexConsumer, -0.5F, 0.5F, 0, 0, v0, packedLight);
            addVertex(pose, normal, vertexConsumer, 0.5F, 0.5F, 0, 1, v0, packedLight);
            addVertex(pose, normal, vertexConsumer, 0.5F, -0.5F, 0, 1, v1, packedLight);

            poseStack.popPose();
        }
    }
}
