package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class GlowSparkParticle extends SparkParticle {
    private final boolean quickShrink;
    private final boolean glow;
    private final Vector2d squashOld;

    public GlowSparkParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, float gravity,
                             int lifeTime, float scale, Vector2d squash, Vector4f color, boolean quickShrink, boolean glow) {
        super(level, x, y, z, xSpeed, ySpeed, gravity, lifeTime, scale, color);
        xd = xSpeed;
        yd = ySpeed;
        zd = 0;

        this.squash.set(squash.x, squash.y);
        this.glow = glow;

        this.quickShrink = quickShrink;
        if (quickShrink) squashOld = new Vector2d(squash.x, squash.y);
        else squashOld = null;
    }

    @Override
    public void tick() {
        super.tick();

        if (quickShrink) {
            squashOld.set(squash.x, squash.y);
            squash.x *= 0.512f;
            squash.y *= 1.95f;
        }

        if (gravity != 0 && Math.sqrt(xd * xd + yd * yd) < 0.12f) {
            xd *= 0.94f;
            yd -= 0.25f;
        }
    }

    @Override
    protected float getHeight(float partialTick, float scaleLerp) {
        if (quickShrink) return (float) Mth.lerp(partialTick, squashOld.y, squash.y) * scaleLerp;
        else return (float) squash.y * scaleLerp;
    }

    @Override
    protected float getWidth(float partialTick, float scaleLerp) {
        if (quickShrink) return (float) Mth.lerp(partialTick, squashOld.x, squash.x) * scaleLerp;
        else return (float) squash.x * scaleLerp;
    }

    @Override
    protected void beforeEndRender(float x, float y, float z, VertexConsumer buffer, Quaternion quaternion, float height, float width) {
        if (glow) {
            resetVertexData(x, y, z, height, width * 0.45f, quaternion);
            byVertexDataBuild(buffer, 255, 255, 255, 255);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GlowSparkProvider extends SingleTexturedProvider<GlowSparkOptions> {
        public GlowSparkProvider(SpriteSet set) {
            super(set);
        }

        @Override
        protected SingleTexturedParticle getParticle(GlowSparkOptions type, ClientLevel level, double x, double y,
                                                     double z, double xSpeed, double ySpeed, double zSpeed) {
            return new GlowSparkParticle(level, x, y, z, xSpeed, ySpeed, (float) zSpeed, type.lifetime, type.scale,
                new Vector2d(type.squishX, type.squishY), type.color, type.quickShrink, type.glow);
        }
    }

    public static class GlowSparkType extends FastParticleType<GlowSparkOptions> {
        public GlowSparkType(boolean overrideLimiter) {
            super(overrideLimiter);
        }

        @Override
        protected GlowSparkOptions getInstance(Object... o) {
            return new GlowSparkOptions((float) o[0], (int) o[1], new Vector4f((int) o[2], (int) o[3],
                (int) o[4], (int) o[5]), (float) o[6], (float) o[7], (boolean) o[8], (boolean) o[9]);
        }

        @Override
        protected void getDeserializer(ArrayList<Class<?>> list) {
            list.add(float.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(int.class);
            list.add(float.class);
            list.add(float.class);
            list.add(boolean.class);
            list.add(boolean.class);
        }
    }

    public static class GlowSparkOptions implements ParticleOptions {
        protected final float scale;
        protected final int lifetime;
        protected final Vector4f color;
        private final float squishX;
        private final float squishY;
        private final boolean glow;
        private final boolean quickShrink;

        public GlowSparkOptions(float scale, int lifetime, Vector4f color, float squishX, float squishY, boolean glow, boolean quickShrink) {
            this.scale = scale;
            this.lifetime = lifetime;
            this.color = color;
            this.squishX = squishX;
            this.squishY = squishY;
            this.glow = glow;
            this.quickShrink = quickShrink;
        }

        @Override
        public GlowSparkType getType() {
            return ParticleRegister.GLOW_SPARK.get();
        }

        @Override
        public String writeToString() {
            return getType().toCommandString(scale, lifetime, (int) color.x(), (int) color.y(),
                (int) color.z(), (int) color.w(), squishX, squishY, glow, quickShrink);
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            getType().toNetwork(buf, scale, lifetime, (int) color.x(), (int) color.y(),
                (int) color.z(), (int) color.w(), squishX, squishY, glow, quickShrink);
        }
    }
}
