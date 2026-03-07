package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShadowsRain extends BaseProjectile {
    public ShadowsRain(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static void of(LivingEntity target, ServerPlayer player, int count) {
        Level level = target.level;
        Vec3 center = target.position().add(0, target.getEyeHeight() / 2, 0);
        float degreePerNeedle = 360f / count;
        for (int i = 0; i < count; i++) {
            Vec3 offset = new Vec3(0, Math.random(), .25f).normalize().scale(target.getBbHeight() + 6.75f)
                .yRot(degreePerNeedle * i * Mth.DEG_TO_RAD);
            Vec3 spawn = center.add(offset);
            Vec3 motion = center.subtract(spawn).normalize();
            ShadowsRain rain = CalamityEntity.SHADOWS_RAIN.get().create(level);
            rain.moveTo(spawn);

            rain.shoot(motion.x, motion.y, motion.z, 1.35f, 0);
            if (player != null)
                rain.setOwner(player);

            level.addFreshEntity(rain);
        }
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(CalamityDamageSource.getMagicProjectile(this), 2);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Render extends EntityRenderer<ShadowsRain> {
        private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/entity/shadows_rain.png");

        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public void render(ShadowsRain entity, float pEntityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();
            Vec3 motion = entity.getDeltaMovement();
            float pitch = (float) Math.toDegrees(Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)));
            float yawRot = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));

            poseStack.mulPose(Vector3f.YP.rotationDegrees(-yawRot));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
            poseStack.translate(0, 0.1, 0);
            poseStack.scale(.2f, .2f, .2f);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            PoseStack.Pose last = poseStack.last();

            RenderUtil.renderTexture(last.pose(), last.normal(), consumer, packedLight);

            poseStack.popPose();
        }

        @Override
        public ResourceLocation getTextureLocation(ShadowsRain pEntity) {
            return TEXTURE;
        }
    }
}
