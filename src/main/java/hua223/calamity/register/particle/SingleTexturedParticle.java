package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public abstract class SingleTexturedParticle extends TextureSheetParticle {
    protected final Vector3f[] vertex = new Vector3f[]{
        new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
    private float[] uv;

    protected SingleTexturedParticle(ClientLevel level, double x, double y, double z) {
        this(level, x, y, z, 0, 0, 0);
    }

    protected SingleTexturedParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    protected void resetVertexData(float x, float y, float z, float height, float width, Quaternion quaternion) {
        vertex[0].set(-height, -width, 0);
        vertex[1].set(-height, width, 0);
        vertex[2].set(height, width, 0);
        vertex[3].set(height, -width, 0);

        vertex[0].transform(quaternion);
        vertex[1].transform(quaternion);
        vertex[2].transform(quaternion);
        vertex[3].transform(quaternion);

        vertex[0].add(x, y, z);
        vertex[1].add(x, y, z);
        vertex[2].add(x, y, z);
        vertex[3].add(x, y, z);

    }

    protected void byVertexDataBuild(VertexConsumer buffer, Vector4f color) {
        byVertexDataBuild(buffer, (int) color.x(), (int) color.y(), (int) color.z(), (int) color.w());
    }

    protected void byVertexDataBuild(VertexConsumer buffer) {
        byVertexDataBuild(buffer, (int) rCol, (int) gCol, (int) bCol, (int) alpha);
    }

    protected void byVertexDataBuild(VertexConsumer buffer, int r, int g, int b, int a) {
        buffer.vertex(vertex[0].x(), vertex[0].y(), vertex[0].z())
            .uv(uv[0], uv[1])
            .color(r, g, b, a)
            .uv2(15728880)
            .endVertex();

        buffer.vertex(vertex[1].x(), vertex[1].y(), vertex[1].z())
            .uv(uv[2], uv[3])
            .color(r, g, b, a)
            .uv2(15728880)
            .endVertex();

        buffer.vertex(vertex[2].x(), vertex[2].y(), vertex[2].z())
            .uv(uv[4], uv[5])
            .color(r, g, b, a)
            .uv2(15728880)
            .endVertex();

        buffer.vertex(vertex[3].x(), vertex[3].y(), vertex[3].z())
            .uv(uv[6], uv[7])
            .color(r, g, b, a)
            .uv2(15728880)
            .endVertex();
    }

    protected int getRotate() {
        return 0;
    }

    private void setRotateUV() {
        int rotate = getRotate();
        if (rotate > 4) rotate = rotate % 4;

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        uv = new float[8];
        uv[0] = u1;
        uv[1] = v1;

        uv[2] = u1;
        uv[3] = v0;

        uv[4] = u0;
        uv[5] = v0;

        uv[6] = u0;
        uv[7] = v1;

        if (rotate != 0) {
            rotate *= 2;
            rotate -= 1;
            final int base = rotate;

            //CWW rotate
            float lastU = uv[rotate];
            float lastV = uv[rotate - 1];
            do {
                rotate -= 2;
                if (rotate < 0) rotate = 7;

                float u = uv[rotate];
                uv[rotate] = lastU;
                lastU = u;

                float v = uv[rotate - 1];
                uv[rotate - 1] = lastV;
                lastV = v;

            } while (rotate != base);
        }
    }

    @Override
    public void pickSprite(SpriteSet sprite) {
        this.sprite = sprite.get(0, 1);
    }

    @OnlyIn(Dist.CLIENT)
    protected static abstract class SingleTexturedProvider<T extends ParticleOptions> implements ParticleProvider<T> {
        private final SpriteSet set;

        public SingleTexturedProvider(SpriteSet set) {
            this.set = set;
        }

        @Override
        public @Nullable Particle createParticle(T type, ClientLevel clientLevel, double v, double v1, double v2, double v3, double v4, double v5) {
            SingleTexturedParticle particle = getParticle(type, clientLevel, v, v1, v2, v3, v4, v5);
            particle.pickSprite(set);
            particle.setRotateUV();
            return particle;
        }

        protected abstract SingleTexturedParticle getParticle(T type, ClientLevel level, double x, double y, double z,
                                                              double xSpeed, double ySpeed, double zSpeed);
    }
}
