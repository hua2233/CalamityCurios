package hua223.calamity.register.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BallisticPoisonCloud extends TextureSheetParticle {
    private int vFrame;
    private final float frame;

    protected BallisticPoisonCloud(ClientLevel level, double x, double y, double z, double xS, double yS, double zS, SpriteSet set) {
        super(level, x, y, z);
        lifetime = 40;
        setSprite(set.get(0, 1));
        frame = (super.getV1() - super.getV0()) / 10f;
        this.xd = xS;
        gravity = 3.0E-6F;
        this.yd = yS + (double)(this.random.nextFloat() / 500.0F);
        this.zd = zS;
        scale(3f);
    }

    @Override
    public void tick() {
        super.tick();
        if (age < 36) vFrame = age / 4;
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        setBoundingBox(getBoundingBox().move(pX, pY, pZ));
        setLocationFromBoundingbox();
    }

    @Override
    protected float getV0() {
        return super.getV0() + frame * vFrame;
    }

    @Override
    protected float getV1() {
        return super.getV0() + frame * (vFrame + 1);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            return new BallisticPoisonCloud(clientLevel, v, v1, v2, v3, v4, v5, sprites);
        }
    }
}
