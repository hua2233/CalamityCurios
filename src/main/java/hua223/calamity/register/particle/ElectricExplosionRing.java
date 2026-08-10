package hua223.calamity.register.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ElectricExplosionRing extends SingleTexturedParticle {
    private final int[] palette;

    protected ElectricExplosionRing(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        xd = 0;
        yd = 0;
        zd = 0;
        lifetime = 40;

        palette = new int[] {
            0xFFFAFF70,
            0xFFD3EB6C,
            0xFFA6F069,
            0xFF69F0DC,
            0xFF408291,
            0xFF916091,
            0xFFF27049,
            0xFFC73E3E
        };
        for (int i = 0; i < palette.length; i++)
            palette[i] = FastColor.ARGB32.lerp(.3f, palette[i], 0xFFFF0000);
    }

    @Override
    public void tick() {
        oRoll = roll;
        roll += (float) Math.PI / 22F;
        super.tick();
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
        int ringCount = 33;
        float scale = 6f;
        float innerRingScaleFactor = Mth.lerp(scale / (scale + 1f), 0.3f, 0f);
        float fadeout = RenderUtil.clampLerp(0f, 3f, age);
        float scaleFadeout = fadeout;
        float lifetimeCompletion = (float) age / getLifetime();

        float lifeTimeLerp = RenderUtil.clampLerp(1f, 0.7f, lifetimeCompletion);
        float opacityFadeout = fadeout * lifeTimeLerp;
        scaleFadeout *= lifeTimeLerp;

        Vec3 position = renderInfo.getPosition();
        float rotation = Mth.lerp(partialTicks, oRoll, roll);
        float xP = (float) (Mth.lerp(partialTicks, xo, x) - position.x);
        float yP = (float) (Mth.lerp(partialTicks, yo, y) - position.y);
        float zP = (float) (Mth.lerp(partialTicks, zo, z) - position.z);

        for (int i = 0; i < ringCount; i++) {
            float size = Mth.lerp(i / (ringCount - 1f), 1f, innerRingScaleFactor) * scaleFadeout * scale;
            resetVertexData(xP, yP, zP, size, size, new Quaternionf(renderInfo.rotation()).mul(
                new Quaternionf().rotateZ(rotation * Mth.lerp(i / (ringCount - 1f), 0.5f, 1f) * (i % 2 == 0 ? 1 : -1))));

            int color = CalamityHelp.multicolorLerp((i / (ringCount - 1f) + RenderUtil.getLocalTick() * 0.03f) % 1f, palette);
            byVertexDataBuild(buffer,
                (int) (FastColor.ARGB32.red(color) * opacityFadeout * 0.15f),
                (int) (FastColor.ARGB32.green(color) * opacityFadeout * 0.15f),
                (int) (FastColor.ARGB32.blue(color) * opacityFadeout * 0.15f),
                (int) (FastColor.ARGB32.alpha(color) * opacityFadeout * 0.4f));
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return RenderUtil.Shaders.GENERIC_BLOOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider extends SingleTexturedProvider<SimpleParticleType> {
        public Provider(SpriteSet set) {
            super(set);
        }

        @Override
        protected SingleTexturedParticle getParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ElectricExplosionRing(level, x, y, z);
        }
    }
}
