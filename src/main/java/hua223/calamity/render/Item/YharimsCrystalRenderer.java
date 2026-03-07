package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;

/**
 * Under construction......
 */
@OnlyIn(Dist.CLIENT)
public class YharimsCrystalRenderer {
    public static boolean crystalRayRender;
    private static Vec3[] endPos;
    private static Vec3[] lastEndPos;
    private static float spinRate;
    private static float lastRotateAngle;
    private static float rotateAngle;
    private static float circleStartAngle;
    private static int tick;
    private static int[] color;
    private static float scale;

    private YharimsCrystalRenderer() {}

    public static void start(LocalPlayer player) {
        crystalRayRender = true;
        endPos = new Vec3[6];
        lastEndPos = new Vec3[6];
        setColor();
        update(player);
        System.arraycopy(endPos, 0, lastEndPos, 0, endPos.length);
        RenderUtil.onlyThirdPersonRender(player, true, true, true, false);
    }

    public static void stop(LocalPlayer player) {
        if (crystalRayRender) {
            crystalRayRender = false;
            endPos = null;
            color = null;
            lastEndPos = null;
            lastRotateAngle = 0;
            rotateAngle = 0;
            spinRate = 0;
            circleStartAngle = 0;
            tick = 0;
            scale = 0.05f;
            RenderUtil.cancelThirdPersonRendering(player);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void update(LivingEntity player) {
        System.arraycopy(endPos, 0, lastEndPos, 0, endPos.length);
        float chargeRatio = Mth.clamp(tick++ / 60f, 0f, 1f);
        float radius;
        if (chargeRatio < 1) {
            radius = Mth.lerp(chargeRatio, 16, 0.05f);
            scale = Mth.lerp(chargeRatio, 0.05f, 0.2f);

            if (chargeRatio <= 0.66f) {
                float phaseRatio = chargeRatio * 1.5f;
                spinRate = Mth.lerp(phaseRatio, 0, 16f);
            } else {
                float phaseRatio = (chargeRatio - 0.66f) * 3f;
                spinRate = Mth.lerp(phaseRatio, 8, 40f);
            }
        } else radius = player.getRandom().nextFloat() * 0.05f;

        lastRotateAngle = rotateAngle;
        if ((rotateAngle += spinRate) > 180) rotateAngle -= 360;

        if ((circleStartAngle += (float) Math.toRadians(spinRate)) > Mth.TWO_PI)
            circleStartAngle -= Mth.TWO_PI;

        Vec3[] dir = CalamityHelp.makeBasisFromDirection(yRotDir(player));
        Vec3 startPos = player.position().add(0, player.getEyeHeight() * 0.7, 0)
            .add(dir[2]);

        for (int i = 0; i < endPos.length; i++) {
            double angle = circleStartAngle + (2 * Math.PI * i / 6);

            Vec3 offset = dir[0].scale(radius * Math.cos(angle))
                .add(dir[1].scale(radius * Math.sin(angle)));

            Vec3 target = startPos.add(dir[2].scale(20f)).add(offset);

            BlockHitResult hit = player.level.clip(new ClipContext(startPos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            endPos[i] = (hit.getType() != HitResult.Type.MISS ? hit.getLocation() : target).subtract(startPos);
        }
    }

    public static BakedModel updateModelTransform(PoseStack pose, BakedModel model, ItemTransforms.TransformType type) {
        ItemTransform transform = model.getTransforms().getTransform(type);
        transform.rotation.setY(Mth.rotLerp(Minecraft.getInstance().getFrameTime(), lastRotateAngle, rotateAngle));
        transform.apply(false, pose);
        return model;
    }

    public static void renderYharimsCrystal(RenderPlayerEvent.Post event) {
        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Player player = event.getEntity();
        float partialTick = event.getPartialTick();

        Vec3 forward = yRotDir(player);
        pose.translate(forward.x, forward.y + player.getEyeHeight() * 0.7f, forward.z);

        for (int i = 0; i < endPos.length; i++) {
            pose.pushPose();
            PoseStack.Pose last = pose.last();

            Vec3 start = Vec3.ZERO;
            Vec3 pos = RenderUtil.slerp(lastEndPos[i], endPos[i], partialTick);
            pose.mulPose(RenderUtil.directionToQuaternion(pos));
            pose.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(partialTick, lastRotateAngle, rotateAngle)));
            float uv = RenderUtil.getLocalTick() * 0.3f;
            VertexConsumer consumer = event.getMultiBufferSource().getBuffer(
                RenderType.energySwirl(CrusherRender.TEXTURE, uv, uv));

            int index = i * 3;
            int r = color[index];
            int g = color[++index];
            int b = color[++index];
            for (float j = 0; j <= endPos[i].length(); j += .5f) {
                Vec3 end = new Vec3(0, 0, j);
                CrusherRender.drawHull(start, end, scale, last, consumer, r, g, b, 255);
                start = end;
            }

            pose.popPose();
        }

        pose.popPose();
    }

    private static Vec3 yRotDir(LivingEntity player) {
        float f = player.getXRot() * Mth.DEG_TO_RAD;
        float f1 = (-player.yBodyRot + 4) * Mth.DEG_TO_RAD;
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    //My math is hopeless
//    private static Vec3 setTranslateFromAxis(Vec3[] dir, PoseStack pose, LivingEntity player) {
//        Vec3 left = dir[0].scale( 1.2 + -player.yBodyRot * 0.015f);
//        Vec3 up = dir[1].scale(0.03);
//        Vec3 forward = dir[2].scale(1.2 + -player.yBodyRot * 0.06f);
//        if (pose != null) {
//            pose.translate(left.x + up.x + forward.x,
//                left.y + up.y + forward.y,
//                left.z + up.z + forward.z);
//            return null;
//        }
//
//        return player.position().add(left.x + up.x + forward.x,
//            left.y + up.y + forward.y,
//            left.z + up.z + forward.z);
//    }

    //Something in the range of red to yellow
    private static void setColor() {
        color = new int[18];
        for (int indexing = 0; indexing < 6; indexing++) {
            float hue = indexing / 6f % 0.12f;
            float sat = 0.66f;
            float llf = 0.53f;
            float c = (1 - Math.abs(2 * llf - 1)) * sat; // chroma
            float hp = hue * 6f; // hue prime
            float x = c * (1 - Math.abs(hp % 2 - 1));
            float m = llf - c / 2;

            float r, g, b;
            if (hp <= 1) { r = c; g = x; b = 0; }
            else if (hp <= 2) { r = x; g = c; b = 0; }
            else if (hp <= 3) { r = 0; g = c; b = x; }
            else if (hp <= 4) { r = 0; g = x; b = c; }
            else if (hp <= 5) { r = x; g = 0; b = c; }
            else { r = c; g = 0; b = x; }

            int red = Math.round((r + m) * 255);
            int green = Math.round((g + m) * 255);
            int blue = Math.round((b + m) * 255);

            int start = indexing * 3;
            color[start] = Math.max(0, Math.min(255, red));
            color[++start] = Math.max(0, Math.min(255, green));
            color[++start] = Math.max(0, Math.min(255, blue));
        }
    }
}
