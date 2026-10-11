package hua223.calamity.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

//Refactoring
@OnlyIn(Dist.CLIENT)
public class CurseFont extends CalamityFont {
    public static final CurseFont INSTANCE = new CurseFont();

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        int width = width(text);
        Matrix4f transientMatrix = RenderUtil.TRANSIENT_MATRIX;
        CurseEnchantmentRenderer renderer = new CurseEnchantmentRenderer(x, y, dropShadow, displayMode,
            color, transientMatrix, packedLightCoords, preRender(width, matrix, transientMatrix, x, y));

        renderer.y = -lineHeight / 2f;
        for (int i = 0; i < 2; i++) {
            renderer.x = -width / 2f;
            drawInternal(text, renderer, backgroundColor);
            renderer.restartBuild();
        }

        //Draw the front text as usual.
        renderer.x = x;
        renderer.y = y;
        renderer.pose.set(matrix);
        renderer.renderDefault = true;
        return drawInternal(text, renderer, backgroundColor);
    }

    private int drawInternal(FormattedCharSequence text, CurseEnchantmentRenderer renderer, int backgroundColor) {
        float initialX = renderer.x;

        if (renderer.dropShadow) {
            renderer.setDropShadowState(true);
            text.accept(renderer);
            renderer.finish(backgroundColor, initialX);
            renderer.pose.translate(.0F, 0.0F, 0.03F);

            renderer.x = initialX;
            renderer.restartBuild();
            renderer.setDropShadowState(false);
        }

        text.accept(renderer);
        initialX = renderer.finish(backgroundColor, initialX);

        if (renderer.dropShadow) {
            initialX += 1;
            renderer.pose.translate(.0F, 0.0F, -0.03F);
        }

        return (int) initialX;
    }

    // Draw the back text as an ominous pulse.
    private float preRender(int width, Matrix4f sourceMatrix, Matrix4f transientMatrix, float x, float y) {
        float backInterpolant = (float) Math.pow((RenderUtil.getLocalTick() + Minecraft.getInstance().getPartialTick()) * 0.03f % 1f, 1.5f);
        float backScale = Mth.lerp(backInterpolant, 1.0f, 1.2f);

        transientMatrix.set(sourceMatrix);
        transientMatrix.translate(x + width / 2f, y + lineHeight / 2f, 0);
        transientMatrix.scale(backScale, backScale, 1.0f);

        return backInterpolant;
    }

    //Based on custom color
    @OnlyIn(Dist.CLIENT)
    private class CurseEnchantmentRenderer extends BaseFontRenderer {
        private boolean renderDefault;
        private final float backInterpolant;

        public CurseEnchantmentRenderer(float x, float y, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light, float backInterpolant) {
            super(x, y, dropShadow, mode, color, pose, light);
            this.backInterpolant = backInterpolant;
        }

        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {

            return super.accept(positionInCurrentSequence, style, codePoint);
        }

        @Override
        protected TextColor getTextColor(Style style) {
            TextColor textColor = style.getColor();
            if (renderDefault) {
                a = 1f;
                return textColor;
            }

            if (textColor != null) {
                int color = ARGB32.lerp(backInterpolant, textColor.getValue(), 0xFF8B0000);
                float colorFactor =  (float) Math.pow(1f - backInterpolant, 0.46f);

                r = ARGB32.red(color) / 255f * colorFactor;
                g = ARGB32.green(color) / 255f * colorFactor;
                b = ARGB32.blue(color) / 255f * colorFactor;
                a = colorFactor;
            }

            return null;
        }

        @Override
        public void overwriteDefaultRenderState() {
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            (renderDefault ? CalamityShaders.XNA_ALPHA_BLEND : CalamityShaders.XNA_ADDITIVE_BLEND).setupRenderState();
        }
    }
}