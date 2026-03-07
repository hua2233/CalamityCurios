package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import hua223.calamity.util.CurveSegment;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

//The texture of this particle is very large, and there are other particles that also have such a large texture.
//If it affects the content or overall smoothness of the entire texture set, perhaps it should be loaded separately?
//I will decide based on the feedback after the release that it should be refactored, but I am a bit lazy...
@OnlyIn(Dist.CLIENT)
public class Pulse extends SingleTexturedParticle {
    private final float originalScale;
    private final float finalScale;
    private final Quaternion rotation;
    private final Vector2d squish;
    private final Vector4f baseColor;
    private final boolean stationary;
    private final Vector4f color = RenderUtil.black();
    //Vertex
    private final Quaternion varQuaternion = Quaternion.ONE.copy();

    private final CurveSegment[] curveSegments = new CurveSegment[]{
        new CurveSegment(CurveSegment.EasingType.POLY_OUT, 0f, 0f, 1f, 4)};
    private float scale;
    private float scaleOld;

    public Pulse(ClientLevel level, double x, double y, double z, Vector4f color, double speedX, double speedY,
                 float originalScale, float finalScale, int lifeTime, float rotation, Vector2d squish) {
        super(level, x, y, z);
        xd = speedX;
        yd = speedY;
        stationary = xd == 0d && yd == 0;

        baseColor = color;
        this.originalScale = originalScale;
        this.finalScale = finalScale;
        scale = originalScale;
        scaleOld = scale;
        lifetime = lifeTime;
        this.squish = squish;
        this.rotation = Vector3f.ZP.rotation(rotation);
        hasPhysics = false;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }

        if (!stationary) {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }

        float lifetimeCompletion = (float) age / lifetime;
        float pulseProgress = RenderUtil.piecewiseAnimation(lifetimeCompletion, curveSegments);

        scaleOld = scale;
        scale = Mth.lerp(pulseProgress, originalScale, finalScale);
        float opacity = (float) Math.sin(Mth.HALF_PI + lifetimeCompletion * Mth.HALF_PI);
        color.set(baseColor.x() * opacity, baseColor.y() * opacity, baseColor.z() * opacity, baseColor.w() * opacity);
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 position = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - position.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - position.y);
        float z = (float)  (this.z - position.z);

        float scaleLerp = Mth.lerp(partialTick, scaleOld, scale);
        float height = (float) (squish.y * scaleLerp);
        float width = (float) (squish.x * scaleLerp);

        //Hey, bro, if you see this comment, take a break, relax your eyes, go ahead, it's allowed by Miku! ~v·)
        Entity entity = camera.getEntity();
        RenderUtil.reuseQuaternions(varQuaternion, Vector3f.YP, -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()));
        varQuaternion.mul(rotation);
        resetVertexData(x, y, z, height, width, varQuaternion);

        byVertexDataBuild(buffer, color);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RenderUtil.Shaders.GENERIC_BLOOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class PulseProvider extends SingleTexturedProvider<PulseOptions> {
        public PulseProvider(SpriteSet set) {
            super(set);
        }

        @Override
        protected SingleTexturedParticle getParticle(PulseOptions type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new Pulse(level, x, y, z, type.color, xSpeed, ySpeed,
                type.originalScale, type.finalScale, type.lifetime, type.rotation, new Vector2d(type.squishX, type.squishY));
        }

    }

    public static class PulseType extends FastParticleType<PulseOptions> {
        public PulseType(boolean overrideLimiter) {
            super(overrideLimiter);
        }

        @Override
        protected PulseOptions getInstance(Object... o) {
            return new PulseOptions((float) o[0], (float) o[1], (float) o[2], (float) o[3], (float) o[4],
                new Vector4f((int) o[5], (int) o[6], (int) o[7], (int) o[8]), (int) o[9]);
        }

        @Override
        protected void getDeserializer(ArrayList<Class<?>> list) {
            list.add(float.class);
            list.add(float.class);
            list.add(float.class);
            list.add(float.class);
            list.add(float.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
        }
    }

    public static class PulseOptions implements ParticleOptions {
        private final float originalScale;
        private final float finalScale;
        private final float rotation;
        private final float squishX;
        private final float squishY;
        private final int lifetime;
        private final Vector4f color;

        public PulseOptions(float originalScale, float finalScale, float rotation, float squishX,
                            float squishY, Vector4f color, int lifetime) {
            this.color = color;
            this.squishY = squishY;
            this.squishX = squishX;
            this.rotation = rotation;
            this.finalScale = finalScale;
            this.originalScale = originalScale;
            this.lifetime = lifetime;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public PulseType getType() {
            return ParticleRegister.PULSE.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            getType().toNetwork(buffer, originalScale, finalScale, rotation, squishX, squishY,
                (int) color.x(), (int) color.y(), (int) color.z(), (int) color.w(), lifetime);
        }

        @Override
        public String writeToString() {
            return getType().toCommandString(originalScale, finalScale, rotation, squishX, squishY,
                (int) color.x(), (int) color.y(), (int) color.z(), (int) color.w(), lifetime);
        }
    }
}
