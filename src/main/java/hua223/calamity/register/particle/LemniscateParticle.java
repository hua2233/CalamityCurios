package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

@OnlyIn(Dist.CLIENT)
@Deprecated(since = "TestClass")
public class LemniscateParticle extends Particle {
    private static final ResourceLocation TRAIL_TEXTURE = CalamityCurios.ModResource("textures/particle/trail.png");
    private static Set<LemniscateParticle> targets;

    private final Vector3d[] trailPositions = new Vector3d[126];
    private final Vector3d t1 = new Vector3d(0, 0, 0);
    private final Vector3d t2 = new Vector3d(0, 0, 0);
    private final Vector3d topAngleVec = new Vector3d(0, 0, 0);
    private final Vector3d bottomAngleVec = new Vector3d(0, 0, 0);
    //RenderMatrix
    private final PoseStack pose = new PoseStack();
    private final Minecraft MINECRAFT = Minecraft.getInstance();
    private final MultiBufferSource.BufferSource bufferSource = MINECRAFT.renderBuffers().bufferSource();
    private final int color = 0x800080;
    private final float effectiveSpeed = 0.05F;
    private int trailPointer = -1;//
    private LivingEntity target;
    private LivingEntity owner;
    private float scale = 0.4F; //缩放因子
    private float maxSize = 2F; //最大缩放因子
    private float sizeGrowth;
    private Vec3 center;
    private Vec3 lastPos = Vec3.ZERO;
    private double time;
    private int speedIncremental;
    private float speed = 0.05F; // 适当的时间步进速度
    private boolean canApply = true;
    private float[] updateTimestamp;
    private byte timestampIndex;
    private float lastPartial;

    public LemniscateParticle(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z, 0, 0, 0);

        rCol = RenderUtil.getRed(color);
        gCol = RenderUtil.getGreen(color);
        bCol = RenderUtil.getBlue(color);
        alpha = 1f;

        this.gravity = 0;
        center = new Vec3(x, y, z);
        sizeGrowth = 2f;

        this.lifetime = 2000;
    }

    public static void addParticle(LivingEntity target, LivingEntity player) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.getPosition().distanceToSqr(target.getX(), target.getY(), target.getZ()) < 1024.0) {
            LemniscateParticle particle = (LemniscateParticle) minecraft.particleEngine.makeParticle(ParticleRegister.GOLDEN_LEMNISCATE.get(),
                target.getX(), target.getY(), target.getZ(), 0, 0, 0);

            if (particle != null) {
                particle.target = target;
                particle.owner = player;
                particle.setMaxSize(target);
                minecraft.particleEngine.add(particle);

                if (targets == null) targets = new HashSet<>();
                targets.add(particle);
            }
        }
    }

    public static void delete(LivingEntity player) {
        if (targets != null) {
            Iterator<LemniscateParticle> iterator = targets.iterator();

            while (iterator.hasNext()) {
                LemniscateParticle particle = iterator.next();
                if (particle.owner == player) {
                    particle.removed = true;
                    iterator.remove();
                }
            }

            if (targets.isEmpty()) targets = null;
        }
    }

    public static void updateNewEntity(int id, LivingEntity player) {
        for (LemniscateParticle particle : targets) {
            if (particle.owner == player) {
                LivingEntity entity = (LivingEntity) player.level.getEntity(id);
                if (entity != null) {
                    particle.target = entity;
                    particle.setMaxSize(entity);
                }
            }
        }
    }

    private void setMaxSize(LivingEntity target) {
        AABB box = target.getBoundingBox();
        maxSize = (float) Math.min((box.maxX - box.minX) + 1, 5);
        if (maxSize < 0.4f) maxSize = 0.4f;
        else sizeGrowth = (maxSize - 0.4f) / 30f;

    }

    @Override
    public void remove() {
        if (target != null) {
            addParticle(owner, target);
            targets.remove(this);
        }

        removed = true;
    }

    /**
     * 根据双纽线方程计算位置
     */
    public Vec3 getLemniscatePosition() {
        // 伯努利双纽线参数方程
        double denominator = 1 + Math.pow(Math.sin(time), 2);
        double x = scale * Math.cos(time) / denominator;
        double y = scale * Math.sin(time) * Math.cos(time) / denominator;
        double z = 0; // 保持在XY平面

        // 应用旋转 - 仅在XY平面旋转，避免复杂的3D旋转影响轨迹观察
        double cosRot = Math.cos(0);
        double sinRot = Math.sin(0);
        double tempX = x * cosRot - y * sinRot;
        double tempY = x * sinRot + y * cosRot;
        x = tempX;
        y = tempY;

        // 相对于中心点的位置
        updateCenter();
        return center.add(x, y, z);
    }

    @Override
    public void tick() {
        updateCenter();
        if (isMove()) {
            setPos(center.x, center.y, center.z);
            lastPos = center;
        } else {
            if (canApply) updateSpeed();
            time = (time + effectiveSpeed) % (2 * Math.PI);
            // 这确保粒子严格按照双纽线方程运动
            Vec3 targetPos = getLemniscatePosition();
            this.x = targetPos.x;
            this.y = targetPos.y;
            this.z = targetPos.z;

            // 重置速度向量，避免累积影响
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;

            // 淡出效果
//        float fade = 1F - age / (float) lifetime;
//        this.trailA = 0.8F * fade;

            tickTrail();

            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    private boolean isMove() {
        return Double.compare(center.x, lastPos.x) == 0 &&
            Double.compare(center.y, lastPos.y) == 0 &&
            Double.compare(center.z, lastPos.z) == 0;
    }

    private void updateCenter() {
        if (target != null) center = target.getBoundingBox().getCenter();
    }

    @Override
    public void render(@NotNull VertexConsumer consumer, @NotNull Camera camera, float partialTick) {
        if (trailPointer > -1) {
            updateFrameInterpolation(partialTick);

            Vec3 cameraPos = camera.getPosition();
            double x = Mth.lerp(partialTick, this.xo, this.x);
            double y = Mth.lerp(partialTick, this.yo, this.y);
            double z = Mth.lerp(partialTick, this.zo, this.z);

            VertexConsumer vertexConsumer = renderPre();
            pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            setTrailRot(camera);

            int lightColor = getLightColor(partialTick);
            int sampleCount = sampleCount();
            List<Vector3d> trailPoints = new ArrayList<>();

            for (int i = 0; i < sampleCount; i++) {
                trailPoints.add(getTrailPosition(i, partialTick));
            }

            if (!trailPoints.isEmpty()) {
                Vector3d drawFrom = new Vector3d(x, y, z);
                PoseStack.Pose pose2 = pose.last();
                Matrix4f matrix4f = pose2.pose();
                Matrix3f matrix3f = pose2.normal();

                for (int i = 0; i < trailPoints.size(); i++) {
                    Vector3d sample1 = trailPoints.get(i);

                    float u1 = i / (float) sampleCount;
                    float u2 = (i + 1) / (float) sampleCount;

                    vertexConsumer.vertex(matrix4f, (float) drawFrom.x + (float) bottomAngleVec.x,
                            (float) drawFrom.y + (float) bottomAngleVec.y,
                            (float) drawFrom.z + (float) bottomAngleVec.z)
                        .color(rCol, gCol, bCol, alpha)
                        .uv(u1, 1F)
                        .overlayCoords(NO_OVERLAY)
                        .uv2(lightColor)
                        .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                        .endVertex();

                    vertexConsumer.vertex(matrix4f, (float) sample1.x + (float) bottomAngleVec.x,
                            (float) sample1.y + (float) bottomAngleVec.y,
                            (float) sample1.z + (float) bottomAngleVec.z)
                        .color(rCol, gCol, bCol, alpha)
                        .uv(u2, 1F)
                        .overlayCoords(NO_OVERLAY)
                        .uv2(lightColor)
                        .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                        .endVertex();

                    vertexConsumer.vertex(matrix4f, (float) sample1.x + (float) topAngleVec.x,
                            (float) sample1.y + (float) topAngleVec.y,
                            (float) sample1.z + (float) topAngleVec.z)
                        .color(rCol, gCol, bCol, alpha)
                        .uv(u2, 0)
                        .overlayCoords(NO_OVERLAY)
                        .uv2(lightColor)
                        .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                        .endVertex();

                    vertexConsumer.vertex(matrix4f, (float) drawFrom.x + (float) topAngleVec.x,
                            (float) drawFrom.y + (float) topAngleVec.y,
                            (float) drawFrom.z + (float) topAngleVec.z)
                        .color(rCol, gCol, bCol, alpha)
                        .uv(u1, 0)
                        .overlayCoords(NO_OVERLAY)
                        .uv2(lightColor)
                        .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                        .endVertex();

                    drawFrom = sample1;
                }
            }

            renderEnd();
        }
    }

    private void updateFrameInterpolation(float partialTick) {
        if (speedIncremental > 0) {
            if (partialTick < lastPartial && timestampIndex != 0) timestampIndex = 0;
            lastPartial = partialTick;

            if (partialTick > updateTimestamp[timestampIndex]) {
                if (timestampIndex == updateTimestamp.length - 1) {
                    timestampIndex = 0;
                    tick();
                    canApply = true;
                } else {
                    timestampIndex++;
                    canApply = false;
                    tick();
                }
            }
        }
    }

    private void updateSpeed() {
        if (speed <= 0.4f) {
            speed += 0.01f;
            int v = (int) (speed / effectiveSpeed);
            if (v > speedIncremental) {
                speedIncremental = v;
                updateTimestamp = new float[v];

                float average = 1f / (v + 1);
                for (int i = 0; i < v; i++) {
                    updateTimestamp[i] = average * (i + 1);
                }
            }

            float step = 1f / (speedIncremental + 1);
            for (int i = 0; i < speedIncremental; i++) {
                updateTimestamp[i] = step * (i + 1);
            }
        }

        if (scale < maxSize) {
            scale += sizeGrowth;
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    private VertexConsumer renderPre() {
        pose.pushPose();
        return bufferSource.getBuffer(RenderType.entityTranslucent(getTrailTexture()));
    }

    private void renderEnd() {
        bufferSource.endBatch();
        pose.popPose();
    }

    private int sampleCount() {
        return trailPositions.length;
    }

    private void setTrailRot(Camera camera) {
        float roll = 0.017453292F * camera.getXRot();

        double cos = Mth.cos(roll);
        double sin = Mth.sin(roll);
        double rotX = -0.125 * sin;
        double rotY = 0.125 * cos;
        bottomAngleVec.x = rotX;
        bottomAngleVec.y = rotY;
        topAngleVec.x = -rotX;
        topAngleVec.y = -rotY;
    }

    private void tickTrail() {
        if (trailPointer == -1)
            for (int i = 0; i < trailPositions.length; i++) trailPositions[i] = new Vector3d(x, y, z);

        if (++this.trailPointer >= this.trailPositions.length) this.trailPointer = 0;
        this.trailPositions[this.trailPointer].set(this.x, this.y, this.z);
    }

    private Vector3d getTrailPosition(int pointer, float partialTick) {
        if (this.removed) partialTick = 1.0F;
        int arrayLength = trailPositions.length;
        int i = Math.floorMod(this.trailPointer - pointer, arrayLength);
        int j = Math.floorMod(i - 1, arrayLength);

        t1.set(this.trailPositions[j]);
        Vector3d vec = this.trailPositions[i];
        t2.set(vec.x - t1.x, vec.y - t1.y, vec.z - t1.z);
        t2.scale(partialTick);
        t1.add(t2);
        return new Vector3d(t1.x, t1.y, t1.z);
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    private ResourceLocation getTrailTexture() {
        return TRAIL_TEXTURE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class GoldenFactory implements ParticleProvider<SimpleParticleType> {

        public GoldenFactory() {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, @NotNull ClientLevel worldIn, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new LemniscateParticle(worldIn, x, y, z);
        }
    }
}
