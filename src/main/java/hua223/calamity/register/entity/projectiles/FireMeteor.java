package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.CalamityDamageSource;
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

import static hua223.calamity.util.RenderUtil.addVertex;

public class FireMeteor extends BaseProjectile {
    private LivingEntity target;
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(CalamityCurios.ModResource("textures/entity/fire_meteor.png"));

    public FireMeteor(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setDamage(25);
        setNoGravity(true);
    }

    public static FireMeteor of(Level level, LivingEntity target, ServerPlayer player) {
        Vec3 pos = new Vec3(
            target.getX() + level.random.nextDouble() * 3,
            target.getY() + level.random.nextInt(6, 12),
            target.getZ() + level.random.nextDouble() * 3
        );
        FireMeteor fireMeteor = CalamityEntity.FIRE_METEOR.get().create(level);
        fireMeteor.target = target;
        fireMeteor.setOwner(player);
        fireMeteor.setPos(pos);
        return fireMeteor;
    }

    @Override
    protected void logic() {
//        // 预测目标移动轨迹
        Vec3 targetPos = target.position();
        Vec3 targetMotion = target.getDeltaMovement();
        Vec3 predictedPos = targetPos.add(targetMotion.scale(10)); // 提前量预测

        // 计算追踪方向（带重力效果）
        Vec3 toTarget = predictedPos.subtract(this.position())
            .normalize().scale(0.5);

        this.setDeltaMovement(toTarget);
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(CalamityDamageSource.getMagicProjectile(this), damage);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<FireMeteor> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(FireMeteor pEntity) {
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
