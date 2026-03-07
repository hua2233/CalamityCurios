package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;

@OnlyIn(Dist.CLIENT)
public class CrusherRender {
    public static final ResourceLocation TEXTURE = CalamityCurios.resource("textures/entity/beacon_beam.png");
    private static final float RADIUS = .12f;
    public static boolean isRendering = false;
    private static boolean isIncremental = true;
    private static float lastTick;
    private static float distance;
    private static int red = 0;

    public static void render(RenderPlayerEvent.Post event) {
        LivingEntity entity = event.getEntity();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource source = event.getMultiBufferSource();

        pose.pushPose();
        pose.translate(0, entity.getEyeHeight() * 0.7f, 0);

        PoseStack.Pose last = pose.last();
        Vec3 start = Vec3.ZERO;

        if (lastTick != RenderUtil.getLocalTick()) {
            lastTick = RenderUtil.getLocalTick();
            Vec3 impact = length(entity, entity.level);
            distance = (float) entity.getEyePosition().distanceTo(impact);
            colorTransform();
        }

        pose.mulPose(Vector3f.YP.rotationDegrees(-entity.getYRot()));
        pose.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
        float f = (float) Math.floorMod((int) lastTick, 40) + event.getPartialTick();
        pose.mulPose(Vector3f.ZP.rotationDegrees(f * 2.25f - 45.0f));
        VertexConsumer consumer = source.getBuffer(RenderType.entityTranslucent(TEXTURE, true));

        for (float i = 0; i <= distance; i += .5f) {
            Vec3 end = new Vec3(0, 0, i);
            drawHull(start, end, CrusherRender.RADIUS, last, consumer, red, 0, 255, 255);
            start = end;
        }

        pose.popPose();
    }


    public static void start(AbstractClientPlayer player) {
        RenderUtil.onlyThirdPersonRender(player, false, false, false, false);
        isRendering = true;
    }

    public static void stop(AbstractClientPlayer player) {
        isRendering = false;
        isIncremental = true;
        lastTick = 0;
        distance = 0f;
        red = 0;
        RenderUtil.cancelThirdPersonRendering(player);
    }

    private static Vec3 length(LivingEntity entity, Level level) {
        Vec3 start = entity.getEyePosition();
        Vec3 end = entity.getLookAngle().normalize().scale(16f).add(start);

        BlockHitResult result = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

        if (result.getType() != HitResult.Type.MISS) {
            return result.getLocation();
        }

        return entity.getLookAngle().scale(16f).add(start);
    }

    public static void drawHull(Vec3 from, Vec3 to, float radius, PoseStack.Pose pose, VertexConsumer consumer,
                                int r, int b, int g, int a) {
        float half = radius * .5f;
        drawQuad(from.subtract(0, half, 0), to.subtract(0, half, 0),
            radius, 0, pose, consumer, r, g, b, a);

        drawQuad(from.add(0, half, 0), to.add(0, half, 0),
            radius, 0, pose, consumer, r, g, b, a);

        drawQuad(from.subtract(half, 0, 0), to.subtract(half, 0, 0),
            0, radius, pose, consumer, r, g, b, a);

        drawQuad(from.add(half, 0, 0), to.add(half, 0, 0),
            0, radius, pose, consumer, r, g, b, a);
    }

    public static void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer,
                                int r, int b, int g, int a) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normal = pose.normal();

        float halfWidth = width * .5f;
        float halfHeight = height * .5f;

        Vec3 direction = to.subtract(from).normalize();
        Vec3 cross = direction.cross(CalamityHelp.UNIT_Y).normalize().scale(halfWidth);

        Vec3 v1 = from.add(cross.x, halfHeight, cross.z);
        Vec3 v2 = from.subtract(cross.x, halfHeight, cross.z);
        Vec3 v3 = to.add(cross.x, halfHeight, cross.z);
        Vec3 v4 = to.subtract(cross.x, halfHeight, cross.z);

        buildVertex(consumer, poseMatrix, normal, v1, 1f, 0f, r, g, b, a);
        buildVertex(consumer, poseMatrix, normal, v2, 0f, 0f, r, g, b, a);
        buildVertex(consumer, poseMatrix, normal, v4, 0f, 1f, r, g, b, a);
        buildVertex(consumer, poseMatrix, normal, v3, 1f, 1f, r, g, b, a);
    }

    private static void buildVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, Vec3 pos,
                                    float u, float v, int r, int g, int b, int a) {
        consumer.vertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).color(r, g, b, a).uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0f, 1f, 0f).endVertex();
    }

    private static void colorTransform() {
        if (isIncremental) {
            red += 5;
            if (red == 150) isIncremental = false;
        } else {
            red -= 5;
            if (red == 0) isIncremental = true;
        }
    }
}
