package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CodeSymbolsSet extends ScreenParticleSet<CodeSymbolsSet.CodeSymbols> {
    protected final int lineHeight;
    protected final RandomSource source;

    public CodeSymbolsSet(RandomSource source, int lineHeight) {
        super(20, 20, 20, 22);
        this.source = source;
        this.lineHeight = lineHeight;
    }

    public final void drawSet(float x, float y, float width, Matrix4f matrix4f, TextureAtlasSprite codeSymbols) {
        if (active.isEmpty()) return;

        float frameU0 = codeSymbols.getU0();
        float frameU1 = codeSymbols.getU1();
        CalamityShaders.XNA_ALPHA_BLEND.setupRenderState();
        RenderSystem.setShader(CalamityShaders.getBaseShader());
        RenderSystem.setShaderTexture(0, codeSymbols.atlasLocation());
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (CodeSymbols symbols : active) {
            float frameV = (codeSymbols.getV1() - codeSymbols.getV0()) / 11f;
            int animationFrame = ((int) Math.floor(symbols.time / (symbols.getLifetime() / 2f)) + symbols.frameOffset) % 10;

            float frameV0 = codeSymbols.getV0() + frameV * animationFrame;
            float frameV1 = frameV0 + frameV;

            float u0 = x + Math.max(2, width * symbols.posX);
            float u1 = u0 + symbols.size;
            float v1 = y + symbols.posY;
            float v0 = v1 - symbols.size;

            float fade = RenderUtil.clampLerp(0, 6, symbols.getLifetime() - symbols.time);
            int r = (int) (224 * fade);
            int gb = (int) (255 * fade);

            builder.vertex(matrix4f, u0, v0, 0).color(r, gb, gb, 0).uv(frameU0, frameV0).endVertex();
            builder.vertex(matrix4f, u0, v1, 0).color(r, gb, gb, 0).uv(frameU0, frameV1).endVertex();
            builder.vertex(matrix4f, u1, v1, 0).color(r, gb, gb, 0).uv(frameU1, frameV1).endVertex();
            builder.vertex(matrix4f, u1, v0, 0).color(r, gb, gb, 0).uv(frameU1, frameV0).endVertex();
        }

        //Batch drawing
        BufferUploader.drawWithShader(builder.end());
    }

    @Override
    protected CodeSymbols getParticle() {
        return new CodeSymbols();
    }

    @Override
    public void update() {
        super.update();
        if (source.nextFloat() < .25f) getNext();
    }

    protected class CodeSymbols extends ScreenParticle {
        private float posY;
        private float posX;
        private float size;
        private int frameOffset;
        private float velocityY;

        protected CodeSymbols() {
            super(null);
            color = null;
        }

        @Override
        protected void update() {
            posY += velocityY;
        }

        @Override
        protected void reActive() {
            time = 0;
            posX = source.nextFloat();
            frameOffset = source.nextInt(50);
            lifetimeRatio = source.nextInt(13, 20);
            size = lineHeight * Mth.randomBetween(source, .3f, .42f);
            posY = (Mth.randomBetween(source, .5f, .92f)) * lineHeight + lineHeight * .22f;
            velocityY = new Vector2f(0, 1).rotatedBy(Mth.randomBetween(source, -0.05f, 0.05f),
                Vector2f.ZERO, true).mul(Mth.randomBetween(source, -0.15f, -0.35f)).y;
        }
    }
}
