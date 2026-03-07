package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class SparkParticle extends SingleTexturedParticle {
    protected final Vector4f color = RenderUtil.black();
    protected final Vector4f initialColor;
    protected final boolean stationary;
    protected float scale;
    protected float scaleOld;
    protected Vector2d squash = new Vector2d(0.5, 1.6);

    public SparkParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, float gravity,
                         int lifeTime, float scale, Vector4f color) {
        super(level, x, y, z, xSpeed, ySpeed, 0);
        xd = xSpeed;
        yd = ySpeed;
        zd = 0;
        stationary = xd == 0d && yd == 0 && gravity == 0;
        this.lifetime = lifeTime;
        this.gravity = gravity;
        this.scale = scale;
        scaleOld = scale;
        initialColor = color;
        roll = (float) (Math.atan2(yd, xd));
        oRoll = roll;
        hasPhysics = false;
        friction = 0.95f;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        scaleOld = scale;
        scale *= 0.95f;

        float lifetimeCompletion = (float) age / lifetime;
        RenderUtil.interpolateColor(initialColor, RenderUtil.TRANSPARENT, (float) Math.pow(lifetimeCompletion, 3d), color);

        if (!stationary) {
            this.xo = this.x;
            this.yo = this.y;

            if (gravity != 0) this.yd -= 0.04 * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= this.friction;
            this.yd *= this.friction;
        }

        oRoll = roll;
        roll = (float) (Math.atan2(yd, xd));
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    protected float getHeight(float partialTick, float scaleLerp) {
        return (float) squash.y * scaleLerp;
    }

    protected float getWidth(float partialTick, float scaleLerp) {
        return (float) squash.x * scaleLerp;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 position = camera.getPosition();
        float x = (float) (stationary ? this.x : Mth.lerp(partialTick, this.xo, this.x) - position.x);
        float y = (float) (stationary ? this.y : Mth.lerp(partialTick, this.yo, this.y) - position.y);
        float z = (float) (this.z - position.z);
        Quaternion quaternion = Vector3f.ZP.rotation(RenderUtil.rotLerpRadians(partialTick, oRoll, roll));

        float scaleLerp = Mth.lerp(partialTick, scaleOld, scale);
        float height = getHeight(partialTick, scaleLerp);
        float width = getWidth(partialTick, scaleLerp);

        resetVertexData(x, y, z, height, width, quaternion);
        byVertexDataBuild(buffer, color);
        beforeEndRender(x, y, z, buffer, quaternion, height, width);
    }

    protected void beforeEndRender(float x, float y, float z, VertexConsumer buffer, Quaternion quaternion, float height, float width) {
        resetVertexData(x, y, z, height * 0.45f, width, quaternion);
        byVertexDataBuild(buffer, color);
    }

    @Override
    protected int getRotate() {
        return 1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RenderUtil.Shaders.GENERIC_BLOOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class SparkProvider extends SingleTexturedProvider<SparkOptions> {
        public SparkProvider(SpriteSet set) {
            super(set);
        }

        @Override
        protected SingleTexturedParticle getParticle(SparkOptions type, ClientLevel level, double x, double y,
                                                     double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SparkParticle(level, x, y, z, xSpeed, ySpeed, (float) zSpeed, type.lifetime, type.scale, type.color);
        }
    }

    public static class SparkType extends FastParticleType<SparkOptions> {
        public SparkType(boolean overrideLimiter) {
            super(overrideLimiter);
        }

        @Override
        protected SparkOptions getInstance(Object... o) {
            return new SparkOptions((Float) o[0], (Integer) o[1], new Vector4f((Integer) o[2], (Integer) o[3], (Integer) o[4], (Integer) o[5]));
        }

        @Override
        protected void getDeserializer(ArrayList<Class<?>> list) {
            list.add(float.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
        }
    }

    public static class SparkOptions implements ParticleOptions {
        protected final float scale;
        protected final int lifetime;
        protected final Vector4f color;

        public SparkOptions(float scale, int lifetime, Vector4f color) {
            this.scale = scale;
            this.lifetime = lifetime;
            this.color = color;
        }

        @Override
        public SparkType getType() {
            return ParticleRegister.SPARK.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            getType().toNetwork(buffer, scale, (int) color.x(), (int) color.y(), (int) color.z(), (int) color.w());
        }

        @Override
        public String writeToString() {
            return getType().toCommandString(scale, lifetime, (int) color.x(), (int) color.y(), (int) color.z(), (int) color.w());
        }
    }
}
