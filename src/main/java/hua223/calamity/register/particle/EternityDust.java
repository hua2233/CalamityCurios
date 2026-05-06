package hua223.calamity.register.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class EternityDust extends TextureSheetParticle {
    protected EternityDust(ClientLevel level, double x, double y, double z, double speedX, double speedY, double speedZ) {
        super(level, x, y, z);
        xd = speedX;
        yd = speedY;
        zd = speedZ;

        lifetime = 45;
        hasPhysics = false;
        friction = 0.92f;
        gravity = 0f;
        super.scale(0.7f);
    }

    @Override
    public void tick() {
        super.tick();

        scale(0.98f);
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return 15728880;
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        setLocationFromBoundingbox();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class DustProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet set;

        public DustProvider(SpriteSet sprite) {
            set = sprite;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            EternityDust dust = new EternityDust(clientLevel, v, v1, v2, v3, v4, v5);
            dust.pickSprite(set);
            return dust;
        }
    }
}
