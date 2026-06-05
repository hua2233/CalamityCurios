package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.register.Items.YharimsCrystal;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
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
    public static Vec3[] endPos;
    public static Vec3[] lastEndPos;
    public static float circleStartAngle;
    public static float scale;
    public static float lastRotateAngle;
    public static float spinRate;
    public static float rotateAngle;
    private static int[] color;

    private YharimsCrystalRenderer() {}

    public static void start(LocalPlayer init) {
        if (init == null) {
            crystalRayRender = true;
            endPos = new Vec3[6];
            lastEndPos = new Vec3[6];
            setColor();
        } else RenderUtil.onlyThirdPersonRender(init, true, true, true, false);
    }

    public static void stop(LocalPlayer player) {
        if (crystalRayRender) {
            crystalRayRender = false;
            endPos = null;
            color = null;
            lastEndPos = null;
            lastRotateAngle = 0;
            rotateAngle = 0;
            circleStartAngle = 0;
            scale = 0.05f;
            spinRate = 0;
            RenderUtil.cancelThirdPersonRendering(player);
        }
    }

    @SuppressWarnings("deprecation")
    public static BakedModel updateModelTransform(PoseStack pose, BakedModel model, ItemDisplayContext type) {
        ItemTransform transform = model.getTransforms().getTransform(type);
        transform.rotation.y = Mth.rotLerp(Minecraft.getInstance().getFrameTime(), lastRotateAngle, rotateAngle);
        transform.apply(false, pose);
        return model;
    }

    public static void renderYharimsCrystal(RenderPlayerEvent.Post event) {
        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Player player = event.getEntity();
        float partialTick = event.getPartialTick();
        Vec3 forward = YharimsCrystal.yRotDir(player);
        pose.translate(forward.x, forward.y + player.getEyeHeight() * 0.7f, forward.z);

        float uv = RenderUtil.getLocalTick() * 0.3f;
        RenderType type = RenderType.energySwirl(CrusherRender.TEXTURE, uv, uv);
        for (int i = 0; i < endPos.length; i++) {
            pose.pushPose();
            PoseStack.Pose last = pose.last();

            Vec3 start = Vec3.ZERO;
            Vec3 pos = RenderUtil.slerp(lastEndPos[i], endPos[i], partialTick);
            pose.mulPose(RenderUtil.directionToQuaternion(pos));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTick, lastRotateAngle, rotateAngle)));
            VertexConsumer consumer = event.getMultiBufferSource().getBuffer(type);

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
