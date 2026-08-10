package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.screen.ConvergingEnergyRenderer;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class LineStreakParticleSet extends ScreenParticleSet<LineStreakParticleSet.LineStreakParticle> {
    private static final float ENDING_SCALE_X = 1.6f;
    private static final float ENDING_SCALE_Y = .015f;
    private static final float STARTING_SCALE_X = 80f;
    private static final float STARTING_SCALE_Y = 5f;

    private final ConvergingEnergyRenderer renderer;
    private final ResourceLocation texture;
    private final Vector4i[] palette = new Vector4i[] {
        new Vector4i(112, 30, 255, 255),
        new Vector4i(246, 240, 177, 255),
        new Vector4i(40, 40, 40, 255)
    };

    public LineStreakParticleSet(ConvergingEnergyRenderer renderer) {
        super(11, 500,300, 200);
        this.renderer = renderer;
        texture = CalamityCurios.ModResource("textures/effect/bloom_circle_small.png");
    }

    @Override
    public void update() {
        double consequent = Math.sqrt(renderer.getEnergyChargeUpCompletion());
        for (int i = 0; i < renderer.getEnergyChargeUpCompletion() * 50f; i++)
            if (renderer.random.nextFloat() <= consequent) getNext();

        //Update and exchange all particles
        super.update();
    }

    @Override
    protected LineStreakParticle getParticle() {
        return new LineStreakParticle();
    }

    public void drawSet() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        boolean addBlendMode = true;
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        float partialTick = renderer.getPartialTick();

        for (LineStreakParticle particle : active) {
            if (particle.isSubtractive != addBlendMode) {
                addBlendMode = particle.isSubtractive;
                RenderSystem.blendFunc(
                    addBlendMode ? GlStateManager.SourceFactor.ONE : GlStateManager.SourceFactor.ZERO,
                    addBlendMode ? GlStateManager.DestFactor.ONE :GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
            }
            Matrix4f matrix = getTransientMatrix();
            matrix.identity();
            matrix.translate(Mth.lerp(partialTick, particle.oldPosition.x, particle.streakPosition.x),
                Mth.lerp(partialTick, particle.oldPosition.y, particle.streakPosition.y), 0);
            matrix.rotate(particle.rotation);
            particle.baseDraw(builder);
        }

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    @OnlyIn(Dist.CLIENT)
    protected class LineStreakParticle extends ScreenParticle {
        private final Vector2f streakPosition = new Vector2f();
        private final Vector2f oldPosition = new Vector2f();
        private final Vector2f streakVelocity = new Vector2f();
        private final Vector2f oldScale = new Vector2f();
        private final Vector2f varScale = new Vector2f();
        private final Quaternionf rotation = new Quaternionf();
        private boolean isSubtractive;

        private LineStreakParticle() {
            super(new Vector2f(STARTING_SCALE_X, STARTING_SCALE_Y));
            reActive();
        }

        @Override
        protected void reActive() {
            streakPosition.nextVector2Unit(renderer.random);
            float completion = renderer.getEnergyChargeUpCompletion();
            //Get Center
            Vector2f center = renderer.getCenter();
            //Move to a relative position
            float radiusX = center.x * (2.5f - completion);
            float radiusY = center.y * (2.5f - completion);
            streakPosition.set(streakPosition.x * radiusX, streakPosition.y * radiusY);
            //Set the center orientation vector
            streakVelocity.set(streakPosition);
            streakVelocity.mul(-0.1);
            streakPosition.add(center);
            //Point to the center and adjust the color
            RenderUtil.radianQuaternions(rotation, CalamityHelp.UNIT_Z, streakVelocity.toRotation());
            fromPalette(palette, completion);
            time = 0;
        }

        @Override
        protected void update() {
            oldScale.set(size);
            if (lifetimeRatio != 1)
                size.set(Mth.lerp(lifetimeRatio, STARTING_SCALE_X, ENDING_SCALE_X), Mth.lerp(lifetimeRatio, STARTING_SCALE_Y, ENDING_SCALE_Y));

            if (lifetimeRatio >= 0.7f) {
                float t = (lifetimeRatio - 0.7f) / 0.3f;
                RenderUtil.multiplyColor(color, 1f - t * 0.15f, color);
            }

            oldPosition.set(streakPosition);
            streakPosition.add(streakVelocity.x, streakVelocity.y);
            streakVelocity.mul(lifetimeRatio <= 0.3f || lifetimeRatio >= 0.75f ? 0.76f : 1.2f);
        }

        @Override
        protected Vector2f getSize() {
            //Perform some interpolation slightly
            Vector2f scale = super.getSize();
            varScale.set(Mth.lerp(renderer.getPartialTick(), oldScale.x, scale.x),
                Mth.lerp(renderer.getPartialTick(), oldScale.y, scale.y));
            return varScale;
        }

        @Override
        protected void fromPalette(Vector4i[] palette, float interpolant) {
            super.fromPalette(palette, interpolant);
            RenderUtil.multiplyColor(color, interpolant, color);
            color.w = 0;
            isSubtractive = color.length() <= 77;
            color.w = 255;
        }
    }
}
