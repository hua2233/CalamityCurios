package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.render.screen.ConvergingEnergyRenderer;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import hua223.calamity.render.primitive.PrimitiveRenderer;
import hua223.calamity.render.primitive.PrimitiveSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4i;

import java.awt.*;

public class ConvergingEnergySet extends ScreenParticleSet<ConvergingEnergySet.ConvergingEnergyParticle> {
    public static final int LIFE_TIME = 60;

    public final Vector2f vector;
    private final ConvergingEnergyRenderer renderer;
    private final PrimitiveSettings settings = new EnergySettings();

    private ConvergingEnergyParticle particle;
    private short globalIdentity;

    public ConvergingEnergySet(ConvergingEnergyRenderer renderer) {
        super(LIFE_TIME, 50, 35, 35);
        this.renderer = renderer;
        vector = new Vector2f(1f, 0.56f);
        vector.safeNormalize(Vector2f.ZERO);
    }

    public void drawSet() {
        Matrix4f matrix4f = getTransientMatrix();
        matrix4f.identity();

        for (ConvergingEnergyParticle convergingEnergyParticle : active) {
            particle = convergingEnergyParticle;
            PrimitiveRenderer.renderTrail(particle.oldPos, settings, 60, matrix4f);
        }

        particle = null;
    }

    @Override
    public void update() {
        if (renderer.time % 2 == 0 && renderer.time < ConvergingEnergyRenderer.ENERGY_CHARGE_UP_TIME + ConvergingEnergyRenderer.IDLE_ENERGY_TIME - 35) {
            float completion = renderer.getEnergyChargeUpCompletion();
            float cubed = completion * completion * completion;
            for (int i = 0; i < 5; i++)
                if (completion > 0.25f && renderer.random.nextFloat() > cubed) getNext();
        }

        super.update();
    }

    @Override
    protected ConvergingEnergyParticle getParticle() {
        return new ConvergingEnergyParticle();
    }

    protected class ConvergingEnergyParticle extends ScreenParticle {
        public float angle;
        public float radius;
        private int tick;

        private final short identity;
        public final Vector2f angleVector = new Vector2f();
        private final Quaternionf rotation = new Quaternionf();
        private final CircleBuffer<Vector2f> oldPos = CircleBuffer.ofFill(100, Vector2f::new);

        protected ConvergingEnergyParticle() {
            super(new Vector2f());
            identity = globalIdentity++;
            reActive();
        }

        @Override
        protected void update() {
            Vector2f center = oldPos.getHead();
            Vector2f flyDestination = renderer.getCenter();

            radius *= 0.96f;
            float erring = aperiodicSin(center.x * 0.0093f + center.y * 0.0041f + tick / 10f) * 0.07f +
                aperiodicSin(center.x  * 0.0045f + center.y * 0.0088f + tick / 8f) * 0.07f;
            angle += erring * Mth.inverseLerp(tick, 0f, 16f);
            Vector2f.toRotationVector2(angle, angleVector);
            RenderUtil.radianQuaternions(rotation, CalamityHelp.UNIT_Z, angle);

            if (radius >= 10f) {
                oldPos.fillNext().set(center.add(angleVector.mul(vector).mul(radius)));
            } else {
                if (time > 20) time = 20;
                if (size.x > 9) size.x = Mth.clamp(size.x - 1, 0, 100);
                if (time <= 20) oldPos.fillNext().set(flyDestination);
            }

            if (tick > 9 && tick < 30)
                color.w = (int) (Mth.lerp(tick, 10, 30) * 255);

            tick++;
        }

        protected  float aperiodicSin(float x) {
            return (float) (Math.cos(x * 3.141) + Math.sin(x * 2.718)) * 0.5f;
        }

        @Override
        protected void reActive() {
            float completion = renderer.getEnergyChargeUpCompletion();
            Vector2f center = renderer.getCenter();
            float energySpawnRadius = 0.6f + renderer.random.nextFloat() * (completion * 0.8f);

            Vector2f position = oldPos.fillNext();
            position.vector2Circular(center.x * 0.15f, center.x * 0.15f, renderer.random)
                .add(Vector2f.nextVector2CircularEdge(center.x * energySpawnRadius,
                    center.y * energySpawnRadius, renderer.random)).add(center);
            
            angle = -position.angleTo(center);
            Vector2f.toRotationVector2(angle, angleVector);
            RenderUtil.radianQuaternions(rotation, CalamityHelp.UNIT_Z, angle);
            this.radius = position.distance(center);
            color.w = 255;
            float wh = 1.2f + renderer.random.nextFloat() * 3.2f;
            size.set(wh, wh);
            time = tick =  0;
        }
    }

    private class EnergySettings extends PrimitiveSettings {
        private final Vector4i[] palette;
        private final MultiBufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();

        public EnergySettings() {
            super(RenderUtil.Shaders.getConvergingGenesisEnergy());
            Vector4i white = RenderUtil.fromColorGet(Color.WHITE);
            palette = new Vector4i[] {
                white,
                new Vector4i(71, 35, 137, 255),
                new Vector4i(120, 60, 231, 255),
                new Vector4i(46, 156, 211, 255),
                white,
                new Vector4i(245, 245, 193, 255),
                white,
            };
        }

        @Override
        public float vertexWidth(float completionRatio) {
            return particle.size.x * Mth.inverseLerp(completionRatio, 0f, 0.4f);
        }

        @Override
        public Vector4i vertexColor(float completionRatio) {
            float lifetimeRatio = particle.lifetimeRatio + completionRatio * 0.4f;
            float hue = particle.identity / 17f;
            hue += Mth.inverseLerp(lifetimeRatio, 0.4f, 0.5f) * 0.35f;
            hue += Mth.inverseLerp(lifetimeRatio, 0.81f, 0.9f) * 0.25f;
            particle.fromPalette(palette, hue % 1f);
            int alpha = particle.color.w;
            RenderUtil.multiplyColor(particle.color, RenderUtil.inverseLerpBump(0f, 0.4f, 0.6f, 0.9f, completionRatio), particle.color);
            RenderUtil.multiplyColor(particle.color, alpha / 255f, particle.color);
            particle.color.set(alpha);
            return particle.color;
        }

        @Override
        public void offset(float completionRatio, Vector2f vertex) {
            vertex.add(particle.size.x / 2f, particle.size.y / 2f);
        }

        @Override
        public int getCapacity() {
            return 3;
        }

        @Override
        public float widthCorrectionRatio() {
            return .2f;
        }

        @Override
        public VertexConsumer getConsumer() {
            return source.getBuffer(shader);
        }
    }
}
