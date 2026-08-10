package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector4i;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

@OnlyIn(Dist.CLIENT)
public class Blood extends SingleTexturedParticle {
    private final Vector4i initialColor;
    private final Vector4i color;
    private float verticalStretch = 1f;
    private float size;
    private final int attenuationAge;

    protected Blood(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        setLifetime(level.random.nextInt(22, 36));
        attenuationAge = (int) (lifetime * 0.4);
        initialColor = RenderUtil.interpolateColor(RenderUtil.fromColorGet(Color.RED),
            new Vector4i(139, 0, 0, 255), level.random.nextFloat(), null);
        RenderUtil.interpolateColor(initialColor, new Vector4i(51, 22, 94, 255), level.random.nextFloat() * 0.65f, initialColor);
        color = new Vector4i(initialColor);

        size = 0.03f + level.random.nextFloat() * 0.03f;
        if (level.random.nextFloat() > 0.7) size *= 2f;
        hasPhysics = false;
    }


    @Override
    public void tick() {
        this.yo = this.y;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            move(0, 0.1f, 0);
            if (age >= attenuationAge) {
                RenderUtil.interpolateToTransparent(initialColor,
                    (float) (age - attenuationAge) / (lifetime - attenuationAge), color);
            }

            verticalStretch = Mth.lerp((float) age / lifetime, 1f, 3.2f);
        }
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 position = camera.getPosition();
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - position.y);

        resetVertexData((float) (x - position.x), y, (float) (z - position.z), size, size * verticalStretch, camera.rotation());
        byVertexDataBuild(buffer, color);
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet set) {
            this.sprites = set;
        }

        @Override
        public @Nullable Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            v += (clientLevel.random.nextFloat() - 0.5f) * 1.7f;
            v += v < 0 ? -0.2f : 0.2f;
            v += clientLevel.random.nextFloat() * 0.5f;
            v2 += (clientLevel.random.nextFloat() - 0.5f) * 1.7f;
            v2 += v2 < 0 ? -0.2f : 0.2f;
            Blood blood = new Blood(clientLevel, v, v1, v2);
            blood.pickSprite(sprites);
            blood.setRotateUV();
            return blood;
        }
    }
}
