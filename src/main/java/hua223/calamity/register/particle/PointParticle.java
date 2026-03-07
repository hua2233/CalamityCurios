package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import hua223.calamity.util.RenderUtil;
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

public class PointParticle extends SingleTexturedParticle {
    private final int r;
    private final int g;
    private final int b;
    private final int a;
    private float xSpeed2D;
    private float ySpeed2D;

    protected PointParticle(ClientLevel level, double x, double y, double z, double xS, double yS,
                            double zS, int r, int g, int b, int a, float xSpeed2D, float ySpeed2D) {
        super(level, x, y, z);
        setPos(x, y, z);
        xd = xS;
        yd = yS;
        zd = zS;
        gravity = 1f;

        lifetime = 5;
        hasPhysics = false;
        friction = 0.95f;
        rCol = r;
        this.r = r;
        gCol = g;
        this.g = g;
        bCol = b;
        this.b = b;
        alpha = a;
        this.a = a;
        this.xSpeed2D = xSpeed2D;
        this.ySpeed2D = ySpeed2D;
        quadSize = 0.2f;
    }

    @Override
    public void tick() {
        quadSize *= 0.95f;
        float intensity = (float) Math.pow((double) age / lifetime, 3d);
        rCol = Mth.lerp(intensity, r, 0);
        gCol = Mth.lerp(intensity, g, 0);
        bCol = Mth.lerp(intensity, b, 0);
        alpha = Mth.lerp(intensity, a, 0f);
        xSpeed2D *= friction;
        ySpeed2D *= friction;
        oRoll = roll;
        roll += (float) (Math.atan2(xSpeed2D, ySpeed2D) + Mth.HALF_PI);
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RenderUtil.Shaders.GENERIC_BLOOM;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 position = camera.getPosition();
        float f = (float) (Mth.lerp(partialTicks, xo, x) - position.x);
        float f1 = (float) (Mth.lerp(partialTicks, yo, y) - position.y);
        float f2 = (float) (Mth.lerp(partialTicks, zo, z) - position.z);
        Quaternion quaternion;
        if (roll == 0.0F) {
            quaternion = camera.rotation();
        } else {
            quaternion = new Quaternion(camera.rotation());
            float f3 = Mth.lerp(partialTicks, oRoll, roll);
            quaternion.mul(Vector3f.ZP.rotation(f3));
        }

        resetVertexData(f, f1, f2, quadSize, quadSize, quaternion);
        byVertexDataBuild(buffer);
        resetVertexData(f, f1, f2, quadSize * .45f, quadSize, quaternion);
        byVertexDataBuild(buffer);
    }

    @OnlyIn(Dist.CLIENT)
    public static class PointProvider extends SingleTexturedProvider<PointOptions> {
        public PointProvider(SpriteSet set) {
            super(set);
        }

        @Override
        protected SingleTexturedParticle getParticle(PointOptions type, ClientLevel level, double x, double y,
                                                     double z, double xSpeed, double ySpeed, double zSpeed) {
            return new PointParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.r, type.g, type.b, type.a, type.xSpeed2D, type.ySpeed2D);
        }
    }

    public static class PointType extends FastParticleType<PointOptions> {
        public PointType(boolean overrideLimiter) {
            super(overrideLimiter);
        }

        @Override
        protected PointOptions getInstance(Object... o) {
            return new PointOptions((int) o[0], (int) o[1], (int) o[2], (int) o[3], (float) o[4], (float) o[5]);
        }

        @Override
        protected void getDeserializer(ArrayList<Class<?>> list) {
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(float.class);
            list.add(float.class);
        }
    }

    public static class PointOptions implements ParticleOptions {
        private final int r;
        private final int g;
        private final int b;
        private final int a;
        private final float xSpeed2D;
        private final float ySpeed2D;

        public PointOptions(int r, int g, int b, int a, float xSpeed2D, float ySpeed2D) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.xSpeed2D = xSpeed2D;
            this.ySpeed2D = ySpeed2D;
        }

        @Override
        public PointType getType() {
            return ParticleRegister.POINT.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            getType().toNetwork(buf, r, g, b, a, xSpeed2D, ySpeed2D);
        }

        @Override
        public String writeToString() {
            return getType().toCommandString(r, g, b, a, xSpeed2D, ySpeed2D);
        }
    }
}
