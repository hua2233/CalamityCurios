package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static hua223.calamity.util.RenderUtil.addVertex;

public class MiniDragonBorn extends BaseProjectile {
    @OnlyIn(Dist.CLIENT)
    private RenderType type = RenderType.entityCutout(CalamityCurios.ModResource("textures/entity/mini_dragon_born.png"));
    public MiniDragonBorn(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.damage = 27;
        setNoGravity(true);
    }

    public static MiniDragonBorn of(Level level, Player player) {
        MiniDragonBorn projectile = CalamityEntity.MINI_DRAGON.get().create(level);

        Vec3 lookAngle = player.getLookAngle();
        Vec3 right = lookAngle.cross(CalamityHelp.UNIT_Y).normalize();

        // 在玩家右前方45度位置生成（0.4距离 + 0.3侧移）
        Vec3 spawnPos = player.getEyePosition()
            .add(lookAngle.scale(0.4))
            .add(right.scale(0.3)); // 侧向偏移量

        projectile.setPos(spawnPos);
        projectile.setOwner(player);
        projectile.shootFromRotation(player,
            player.getXRot(),
            player.getYRot(),
            0.0F,
            1.0F, // 初始速度
            1.0F  // 精准度
        );
        return projectile;
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(DamageSource.LIGHTNING_BOLT/*.setProjectile()*/, damage);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<MiniDragonBorn> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(MiniDragonBorn pEntity) {
            return CalamityCurios.ModResource("textures/entity/mini_dragon_born.png");
        }

        @Override
        public void render(MiniDragonBorn entity, float pEntityYaw, float pPartialTick, PoseStack poseStack,
                           MultiBufferSource pBuffer, int packedLight) {
            poseStack.pushPose();

            poseStack.translate(0, 0.2, 0);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

            int frameIndex = (entity.tickCount / 2) % 2;

            float v0 = frameIndex * 0.5f;
            float v1 = v0 + 0.5f;

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
