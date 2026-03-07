package hua223.calamity.register.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class Sakura extends TextureSheetParticle {
    protected Sakura(ClientLevel level, double x, double y, double z, double speedX, double speedY, double speedZ, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = speedX;
        this.yd = speedY;
        this.zd = speedZ;
        gravity = 0f;
        friction = 0.9f;
        lifetime = 20;
        hasPhysics = false;
        float random = level.random.nextFloat();
        rCol = Mth.lerp(random, 153f, 205f) / 255f;
        gCol = Mth.lerp(random, 50f, 92f) / 255f;
        bCol = Mth.lerp(random, 204f, 92f) / 255f;
        scale(level.random.nextInt(30, 50) / 100f);
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }

        scale(0.96f);
        if (this.age > this.lifetime / 2)
            this.setAlpha(1.0F - ((float) this.age - (float) (this.lifetime / 2)) / (float) this.lifetime);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            return new Sakura(clientLevel, v, v1, v2, v3, v4, v5, sprite);
        }
    }
}
