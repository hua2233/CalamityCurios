package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.entity.AutoEntityRegister;
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
import org.jetbrains.annotations.NotNull;

@AutoEntityRegister(sized = {.2f, .2f}, trackingRange = 12)
public class ShadowsRain extends BaseProjectile {
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutoutNoCull(
        CalamityCurios.ModResource("textures/entity/shadows_rain.png"));

    public ShadowsRain(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static void of(LivingEntity target, ServerPlayer player, int count) {
        Level level = target.level();
        Vec3 center = target.position().add(0, target.getEyeHeight() / 2, 0);
        float degreePerNeedle = 360f / count;
        for (int i = 0; i < count; i++) {
            Vec3 offset = new Vec3(0, Math.random(), .25f).normalize().scale(target.getBbHeight() + 6.75f)
                .yRot(degreePerNeedle * i * Mth.DEG_TO_RAD);
            Vec3 spawn = center.add(offset);
            Vec3 motion = center.subtract(spawn).normalize().scale(2);
            ShadowsRain rain = CalamityCurios.getEntityType(ShadowsRain.class).create(level);
            if (rain == null) return;

            rain.moveTo(spawn);
            rain.shoot(motion.x, motion.y, motion.z, 1.35f, 0);
            if (player != null)
                rain.setOwner(player);

            level.addFreshEntity(rain);
        }
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(DamageSupplier.MAGIC_PROJECTILE.get(this, getOwner()), 2);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Renderer extends EntityRenderer<ShadowsRain> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(@NotNull ShadowsRain entity, float pEntityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();
            Vec3 motion = entity.getDeltaMovement();

            float yaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
            float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontal));
            pitch = Mth.clamp(pitch, -90F, 90F);

            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0, 0.1, 0);
            poseStack.scale(.2f, .2f, .2f);

            VertexConsumer consumer = buffer.getBuffer(entity.type);
            PoseStack.Pose last = poseStack.last();
            RenderUtil.renderTexture(last.pose(), last.normal(), consumer, packedLight);
            poseStack.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull ShadowsRain entity) {
            return CalamityCurios.ModResource("textures/entity/shadows_rain.png");
        }
    }
}
