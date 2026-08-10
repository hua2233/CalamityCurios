package hua223.calamity.register.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.CalamityCelestialBodyShader;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.render.primitive.PrimitiveRenderer;
import hua223.calamity.render.primitive.PrimitiveSettings;
import hua223.calamity.render.primitive.VertexArgumentWrapper;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class StormLightning extends Particle {
    public float accumulatedXMovementSpeeds;

    private final LightningSettings settings = new LightningSettings(random.nextInt());

    private final CircleBuffer<Vector2f> oldPos = new CircleBuffer<>(50);

    private final float rawX = (float) x;

    private final double endY;

    private float oAlpha;

    protected StormLightning(ClientLevel level, double x, double y, double z, double endY) {
        super(level, x, y, z);
        if (endY + 3 >= y)
            throw new IllegalArgumentException("There should be at least three blocks between the endpoint and the starting point");
        this.endY = endY - 2d;
        yd = (this.endY - y) / oldPos.size;
        xd = 0;

        lifetime = 40;
        gravity = 0;
        friction = 1F;
        alpha = oAlpha;
        hasPhysics = false;
        level.playLocalSound(x, endY, z, CalamitySounds.LIGHTNING_STRIKE.get(), SoundSource.VOICE, .3f, 1f, false);
    }

    @Override
    public void tick() {
        if (oldPos.isFull()) {
            if (age++ >= lifetime) remove();
        } else{
            xo = x;
            yo = y;
            zo = z;
            age++;
            for (int i = 0; i < 5; i++) {
                if (random.nextFloat() < .3f) {
                    int turnTries = 0;
                    float horizontalBudget = (float) -yd;
                    float lightningTurnRandomnessFactor = .3f;
                    Vector2f newBaseDirection = null;
                    Vector2f potentialBaseDirection;

                    do {
                        potentialBaseDirection = Vector2f.toRotationVector2(random.nextFloat() * Mth.TWO_PI);

                        // Ensure that the new potential direction base is always moving upwards (this is supposed to be somewhat similar to a -UnitY + RotatedBy).
                        potentialBaseDirection.y = -Math.abs(potentialBaseDirection.y);


                        if (potentialBaseDirection.y <= -0.02f && Math.abs(potentialBaseDirection.x * 2f
                            * horizontalBudget + accumulatedXMovementSpeeds) <= lightningTurnRandomnessFactor) newBaseDirection = potentialBaseDirection;

                        turnTries++;
                    } while (newBaseDirection == null && turnTries < 100);

                    if (newBaseDirection != null) {
                        accumulatedXMovementSpeeds += newBaseDirection.x * 2f * horizontalBudget;
                        xd = newBaseDirection.x * horizontalBudget;
                    } else xd *= 0.5f;
                }

                move(xd, Math.max(yd, endY - y), 0);
                oldPos.push(new Vector2f(x, y));
            }
        }

        oAlpha = alpha;
        float timeLife = lifetime - age;
        alpha = RenderUtil.clampLerp(0f, 3f, timeLife) * RenderUtil.clampLerp(lifetime, lifetime - 3f, timeLife);
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        setLocationFromBoundingbox();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public void render(@NotNull VertexConsumer vertexConsumer, @NotNull Camera camera, float partialTick) {
        settings.cameraPos = camera.getPosition();
        settings.partialAlpha = Mth.lerp(partialTick, oAlpha, alpha);
        settings.lerp.set(rawX, Mth.lerp(partialTick, yo, y), z);
        RenderUtil.reuseQuaternions(settings.varQuaternion, CalamityHelp.UNIT_Y, -camera.getYRot());
        PrimitiveRenderer.renderTrail(oldPos, settings, 35, RenderUtil.TRANSIENT_MATRIX);
        if (settings.builder != null) BufferUploader.drawWithShader(settings.builder.end());
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            return new StormLightning(clientLevel, v, v1, v2, v3);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class LightningSettings extends PrimitiveSettings {
        private final Vector4i color = new Vector4i();
        private final Vector3f lerp = new Vector3f();
        private final Quaternionf varQuaternion = new Quaternionf();

        private final int identity;
        private final int[] palette = new int[] {0xFFFF0000, 0xFFFFC800, 0xFFFFAEAE, 0xFFCD5C5C};

        private Vec3 cameraPos;
        private float partialAlpha;
        private BufferBuilder builder;

        public LightningSettings(int i) {
            super(null);
            identity = i;
        }

        @Override
        public float vertexWidth(float completionRatio) {
            return (1f - Math.abs(2f * completionRatio - 1f)) * partialAlpha * 1f;
        }

        @Override
        public Vector4i vertexColor(float completionRatio) {
            int color = CalamityHelp.multicolorLerp((float) (Math.sin(identity / 3f + completionRatio * 20f + RenderUtil.getLocalTick() * .3f) * 0.5f + 0.5f), palette);
            return this.color.set(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), (int) (partialAlpha * 255));
        }

        @Override
        public int getCapacity() {
            return 3;
        }

        @Override
        public float widthCorrectionRatio() {
            return 8;
        }

        @Override
        protected void buildVertex(VertexConsumer consumer, Matrix4f matrix4f, short index) {
            VertexArgumentWrapper vertex = wrappersBuffer[index];
            matrix4f.identity();
            matrix4f.translate( (float) (lerp.x - cameraPos.x), (float) (lerp.y - cameraPos.y), (float) (lerp.z - cameraPos.z));
            matrix4f.rotate(varQuaternion);

            consumer.vertex(matrix4f, vertex.position.x - lerp.x, vertex.position.y - lerp.y, 0f)
                .color(vertex.r, vertex.g, vertex.b, vertex.a)
                .uv(vertex.uv.x, vertex.uv.y)
                .uv2(vertex.w)
                .endVertex();
        }

        @Override
        public BufferBuilder getConsumer() {
            builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(RenderUtil.Shaders.getScarletLightningShader());
            RenderSystem.setShaderTexture(0, CalamityCelestialBodyShader.PERLIN);
            return builder;
        }

        @Override
        public boolean smoothen() {
            return false;
        }
    }
}
