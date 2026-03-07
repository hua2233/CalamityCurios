package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.RenderUtil;
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

public class AcidicRain extends BaseProjectile {
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutoutNoCull(
        CalamityCurios.ModResource("textures/entity/acidic_rain.png"));
    public AcidicRain(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setDamage(3);
        this.setDeltaMovement(0, -0.4, 0);
    }

    public static AcidicRain of(Level level, LivingEntity target, ServerPlayer player) {
        AcidicRain acidicRain = CalamityEntity.ACIDIC_RAIN.get().create(level);
        Vec3 pos = new Vec3(target.getX(), target.getY() + level.random.nextInt(6, 9), target.getZ());
        acidicRain.setPos(pos);
        acidicRain.setOwner(player);
        return acidicRain;
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(CalamityDamageSource.getMagicProjectile(this), damage);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<AcidicRain> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(AcidicRain pEntity) {
            return CalamityCurios.ModResource("textures/entity/acidic_rain.png");
        }

        @Override
        public void render(AcidicRain entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();

            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.translate(0, 0.1f, 0);
            poseStack.scale(0.2f, 0.2f, 0.2f);

            VertexConsumer consumer = buffer.getBuffer(entity.type);
            PoseStack.Pose last = poseStack.last();

            RenderUtil.renderTexture(last.pose(), last.normal(), consumer, packedLight);

            poseStack.popPose();
        }
    }
}
