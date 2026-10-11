package hua223.calamity.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.awt.*;
import java.lang.Math;

import static hua223.calamity.render.shaders.CalamityShaders.MAIN;

//To be refactored...
@OnlyIn(Dist.CLIENT)
public final class RenderUtil {
    public static final Matrix4f TRANSIENT_MATRIX = new Matrix4f();

    public static final HumanoidModel.ArmPose HOLD_POSE = HumanoidModel.ArmPose.create(
        "HOLD", true, (model, entity, arm) -> {
            float xRot = (entity.getXRot() + 8) * Mth.DEG_TO_RAD;
            float armXRot = xRot - 1.5708f;
            model.rightArm.xRot = armXRot;
            model.leftArm.xRot = armXRot;

            float offsetsYRot = xRot * 0.4f;

            model.rightArm.yRot = -0.6981f + offsetsYRot;
            model.leftArm.yRot = 0.6981f + offsetsYRot;
        });


    private static short tick;

    private RenderUtil() {
    }

    public static void updateGlobal() {
        if (tick++ == 3600) tick = 0;
    }

    public static short getLocalTick() {
        return tick;
    }

    public static int processingCycleTime(int startTime) {
        int localTick = tick;
        if (localTick < startTime) localTick += 3600;
        return localTick - startTime;
    }

    public static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer,
                                 float x, float y, float z, float u, float v, int light) {
        consumer.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    public static void renderTexture(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, int light) {
        addVertex(pose, normal, consumer, 0.5f, 0.5f, 0.0f, 1f, 0f, light);
        addVertex(pose, normal, consumer, -0.5f, 0.5f, 0.0f, 0f, 0f, light);
        addVertex(pose, normal, consumer, -0.5f, -0.5f, 0.0f, 0f, 1f, light);
        addVertex(pose, normal, consumer, 0.5f, -0.5f, 0.0f, 1f, 1f, light);
    }

    public static void crossTextureRendering(Projectile projectile, VertexConsumer consumer, PoseStack poseStack, int packedLight) {
        poseStack.mulPose(Axis.YP.rotationDegrees(projectile.tickCount * 6));
        PoseStack.Pose last = poseStack.last();
        renderTexture(last.pose(), last.normal(), consumer, packedLight);

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderTexture(last.pose(), last.normal(), consumer, packedLight);
    }

    public static void renderItemModelList(ItemRenderer renderer, BakedModel model, ItemStack stack,
                                           PoseStack pose, MultiBufferSource source, int combinedLight, int combinedOverlay) {
        boolean fabulous = Minecraft.useFancyGraphics();
        for (BakedModel bakedModel : model.getRenderPasses(stack, fabulous)) {
            for (RenderType type : bakedModel.getRenderTypes(stack, fabulous)) {
                VertexConsumer consumer = ItemRenderer.getFoilBuffer(source, type, true, stack.hasFoil());
                renderer.renderModelLists(bakedModel, stack, combinedLight, combinedOverlay, pose, consumer);
            }
        }
    }

    public static void switchScreenRenderState(RenderTarget target) {
        RenderSystem.backupProjectionMatrix();
        GlStateManager._depthMask(false);
        GlStateManager._colorMask(true, true, true, false);
        RenderSystem.setProjectionMatrix(RenderUtil.TRANSIENT_MATRIX.setOrtho(0.0F,
            target.width, target.height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        mv.last().pose().identity().translation(0.0F, 0.0F, -2000.0F);
        RenderSystem.applyModelViewMatrix();
        GlStateManager._enableBlend();
        GlStateManager._disableDepthTest();
        MAIN.bindWrite(false);
    }

    public static void endScreenRenderState() {
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        RenderSystem.defaultBlendFunc();
        GlStateManager._enableDepthTest();
    }

    public static void blitTo(RenderTarget target) {
        switchScreenRenderState(target);
        float f = target.width;
        float f1 = target.height;
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(0.0F, f1, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(f, f1, 0.0F).color(255, 255, 255, 255).uv(1F, 0.0F).endVertex();
        bufferbuilder.vertex(f, 0.0F, 0.0F).color(255, 255, 255, 255).uv(1F, 1F).endVertex();
        bufferbuilder.vertex(0.0F, 0.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        endScreenRenderState();
    }

    public static Vector2f directionTo(Vec3 vec3, double x, double y) {
        double dx = vec3.x - x;
        double dy = vec3.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            return new Vector2f(0, 0);
        }

        return new Vector2f(dx / length, dy / length);
    }

    public static Vector4i black() {
        return new Vector4i(0, 0, 0, 255);
    }

    public static Vector4i fromColorGet(Color color) {
        return new Vector4i(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int colorSwap(int firstColor, int secondColor, int seconds) {
        float timeMult = Mth.TWO_PI / (seconds * 20);
        float colorMePurple = (float)((Math.sin(timeMult * tick) + 1) * 0.5f);
        return FastColor.ARGB32.lerp(colorMePurple, firstColor, secondColor);
    }

    //handler r, g, b color component and reset a
    public static Vector4i interpolateColor(Vector4i start, Vector4i end, float delta, Vector4i container) {
        int red = (int) Mth.lerp(delta, start.x(), end.x());
        int green = (int) Mth.lerp(delta, start.y(), end.y());
        int blue = (int) Mth.lerp(delta, start.z(), end.z());

        if (container != null) {
            container.set(red, green, blue, 255);
            return container;
        }

        return new Vector4i(red, green, blue, 255);
    }

    public static void interpolateToTransparent(Vector4i start, float delta, Vector4i container) {
        container.x = (int) Mth.lerp(delta, start.x, 0);
        container.y = (int) Mth.lerp(delta, start.y, 0);
        container.z = (int) Mth.lerp(delta, start.z, 0);
        container.w = (int) Mth.lerp(delta, start.w, 0);
    }

    public static Vector4i multiplyColor(Vector4i color, float factor, Vector4i container) {
        container.set((int) Mth.clamp(color.x() * factor, 0, 255), (int) Mth.clamp(color.y() * factor, 0, 255),
            (int) Mth.clamp(color.z() * factor, 0, 255), (int) Mth.clamp(color.w() * factor, 0, 255));
        return container;
    }

    public static float cos01(float x) {
        return (float) (Math.cos(x * Mth.TWO_PI) * 0.5 + 0.5);
    }

    public static float clampLerp(float from, float to, float t) {
        if (from < to) {
            if (t < from) {
                return 0f;
            }
            if (t > to) {
                return 1f;
            }
        } else {
            if (t < to) {
                return 1f;
            }
            if (t > from) {
                return 0f;
            }
        }
        return (t - from) / (to - from);
    }

    public static void reuseQuaternions(Quaternionf quaternion, Vec3 vec3, float degrees) {
        radianQuaternions(quaternion, vec3, degrees * ((float) Math.PI / 180F));
    }

    public static void radianQuaternions(Quaternionf quaternion, Vec3 vec3, float radian) {
        float f = (float) Math.sin(radian / 2.0d);
        quaternion.set((float) (vec3.x * f), (float) (vec3.y * f), (float) (vec3.z * f), (float) Math.cos(radian / 2f));
    }

    public static float smoothStep(float from, float to, float amount) {
        return hermite(from, 0f, to, 0f, Mth.clamp(amount, 0f, 1f));
    }

    public static float inverseLerpBump(float start, float riseEnd, float plateauEnd, float end, float value) {
        if (value < start) return 0;
        if (value > end) return 0;

        if (value <= riseEnd)
            return Mth.inverseLerp(value, start, riseEnd);

        if (value <= plateauEnd)
            return 1;

        return Mth.inverseLerp(value, end, plateauEnd);
    }

    public static float hermite(float value1, float tangent1, float value2, float tangent2, float amount) {
        float cubed = amount * amount * amount;
        float squared = amount * amount;

        if (amount == 0f) return value1;
        else if (amount == 1f) return value2;
        else return (2f * value1 - 2f * value2 + tangent2 + tangent1) * cubed +
                (3f * value2 - 3f * value1 - 2f * tangent1 - tangent2) * squared +
                tangent1 * amount + value1;
    }

    public static Vector2f catmullRomVec(Vector2f value1, Vector2f value2, Vector2f value3, Vector2f value4, float amount) {
        float squared = amount * amount;
        float cubed = amount * squared;
        return new Vector2f(
            0.5f * ((2.0f * value2.x) + (-value1.x + value3.x) *
                amount + (2.0f * value1.x - 5.0f * value2.x + 4.0f * value3.x - value4.x)
                * squared + (-value1.x + 3.0f * value2.x - 3.0f * value3.x + value4.x) * cubed),

            0.5f * ((2.0f * value2.y) + (-value1.y + value3.y) *
                amount + (2.0f * value1.y - 5.0f * value2.y + 4.0f * value3.y - value4.y)
                * squared + (-value1.y + 3.0f * value2.y - 3.0f * value3.y + value4.y) * cubed));
    }

    public static float rotLerpRadians(float delta, float start, float end) {
        return start + delta * wrapRadians(end - start);
    }

    public static float wrapRadians(float radians) {
        float result = radians % Mth.TWO_PI;
        if (result >= Mth.PI) result -= Mth.TWO_PI;
        else if (result < -Mth.PI) result += Mth.TWO_PI;
        return result;
    }

    public static Vec3[] sampleRadialPosAndTangentVel(Vec3[] axis, RandomSource source, Vector2f direction,
                                                      float pi, double radius, double minSpeed, double maxSpeed) {
        Vector2f pos2D = direction.rotatedByRandom(source, Vector2f.ZERO, pi, false).mul(radius);
        Vec3[] posAndVelocity = new Vec3[2];
        posAndVelocity[0] = sampleRadialPos(axis, source, null, pi, pos2D, radius);
        Vector2f speed = pos2D.rotatedBy(Mth.HALF_PI, Vector2f.ZERO, true);

        Vec3 velocityDir = axis[0].scale(speed.x).add(axis[1].scale(speed.y));
        double speedValue = minSpeed + source.nextDouble() * (maxSpeed - minSpeed);
        posAndVelocity[1] = velocityDir.scale(speedValue);
        return posAndVelocity;
    }

    public static Vec3 sampleRadialPos(Vec3[] axis, RandomSource source, Vector2f direction, float pi, Vector2f pos, double radius) {
        Vector2f pos2D = pos == null ? direction.rotatedByRandom(source, Vector2f.ZERO, pi, false).mul(radius) : pos;
        return mapToRelativePlaneCoordinates(axis, pos2D);
    }

    public static Vec3 mapToRelativePlaneCoordinates(Vec3[] axis, Vector2f pos2D) {
        return axis[0].scale(pos2D.x).add(axis[1].scale(pos2D.y));
    }

    public static float angleLerp(float	curAngle, float	targetAngle, float amount) {
        float angle;
        if (targetAngle < curAngle) {
            float num = targetAngle + (float)Math.PI * 2f;
            angle = Mth.lerp(amount, curAngle, (num - curAngle > curAngle - targetAngle) ? targetAngle : num);
        } else {
            if (!(targetAngle > curAngle)) {
                return curAngle;
            }
            float num2 = targetAngle - (float)Math.PI * 2f;
            angle = Mth.lerp(amount, curAngle, (targetAngle - curAngle > curAngle - num2) ? num2 : amount);
        }
        return Mth.wrapDegrees(angle);
    }

    public static Quaternionf directionToQuaternion(Vec3 direction, Quaternionf quaternionf) {
        Vec3 dir = direction.normalize();

        Vector3f defaultForward = new Vector3f(0f, 0f, 1f);
        Vector3f target = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);

        float dot = defaultForward.dot(target);
        if (dot > 0.9999f) return quaternionf.identity(); // 单位四元数

        if (dot < -0.9999f) return quaternionf.identity().rotateAxis(Mth.PI, new Vector3f(0, 1, 0));

        Vector3f axis = new Vector3f(
            defaultForward.y() * target.z() - defaultForward.z() * target.y(),
            defaultForward.z() * target.x() - defaultForward.x() * target.z(),
            defaultForward.x() * target.y() - defaultForward.y() * target.x()
        );

        axis.normalize();
        float angleRad = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, dot)));

        return quaternionf.identity().rotateAxis(angleRad, axis);
    }

    public static Vec3 slerp(Vec3 start, Vec3 end, float t) {
        Vec3 v0 = start.normalize();
        Vec3 v1 = end.normalize();

        double dot = v0.dot(v1);
        dot = Math.max(-1.0, Math.min(1.0, dot));

        if (dot > 0.9995) return v0.scale(1 - t).add(v1.scale(t)).normalize();

        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v1.subtract(v0.scale(dot)).normalize();

        return v0.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }
}
