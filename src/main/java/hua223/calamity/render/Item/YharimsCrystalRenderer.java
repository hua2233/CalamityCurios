package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.YharimsCrystal;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;

//Now, it depends on the player rather than the overall situation to avoid affecting multiple people
@OnlyIn(Dist.CLIENT)
public class YharimsCrystalRenderer implements IPrismRender {
    public final ResourceLocation TEXTURE =
        CalamityCurios.resource("textures/entity/beacon_beam.png");
    private final AbstractClientPlayer player;
    public final Vec3[] endPos;
    public final Vec3[] lastEndPos;
    public float circleStartAngle;
    public float scale = .05f;
    public float lastRotateAngle;
    public float spinRate;
    public float rotateAngle;
    private final int[] color;

    public YharimsCrystalRenderer(AbstractClientPlayer player) {
        this.player = player;
        endPos = new Vec3[6];
        lastEndPos = new Vec3[6];
        color = new int[18];
        setColor();
        onlyThirdPersonRender(true, true, true, false);
    }

    @Override
    public void onStop() {
        cancelThirdPersonRendering();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateModelTransform(PoseStack pose, BakedModel model, ItemDisplayContext type) {
        ItemTransform transform = model.getTransforms().getTransform(type);
        transform.rotation.y = Mth.rotLerp(Minecraft.getInstance().getFrameTime(), lastRotateAngle, rotateAngle);
        transform.apply(false, pose);
    }

    @Override
    public void render(RenderPlayerEvent.Post event) {
        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Player player = event.getEntity();
        float partialTick = event.getPartialTick();
        Vec3 forward = YharimsCrystal.yRotDir(player);
        pose.translate(forward.x, forward.y + player.getEyeHeight() * 0.7f, forward.z);

        float uv = RenderUtil.getLocalTick() * 0.3f;
        RenderType type = RenderType.energySwirl(TEXTURE, uv, uv);
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

    @Override
    public AbstractClientPlayer getOwner() {
        return player;
    }

    //Something in the range of red to yellow
    private void setColor() {
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
