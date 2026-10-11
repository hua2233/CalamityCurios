package hua223.calamity.render.font;

import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class BurnishedAuricFont extends CalamityFont {
    private int lastFlashTime;
    private boolean isFlashing;
    public static final BurnishedAuricFont INSTANCE = new BurnishedAuricFont();

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        if (RenderUtil.processingCycleTime(lastFlashTime) > 6) {
            if (random.nextFloat() < 0.002f) {
                isFlashing = true;
                lastFlashTime = RenderUtil.getLocalTick();
            } else isFlashing = false;
        }

        Vector4i glowColor = new Vector4i();
        if (isFlashing) {
            Vector2f vector2f = new Vector2f().vector2Circular(4f, 2.4f, random);
            x += vector2f.x;
            y += vector2f.y;
            glowColor.set(0, 183, 241, 255);
        } else glowColor.set(157, 110, 11, 255);

        BurnishedAuricFontRenderer renderer = new BurnishedAuricFontRenderer(x, y, DisplayMode.NORMAL, color, packedLightCoords);
        float result = drawBaseTooltipTextAndGlow(text, x, backgroundColor, renderer, matrix, glowColor, 0xFF4D0021, new Vector2f(0.4f, 0));
        drawShineLight(text, x, y, glowColor, backgroundColor, renderer);

        return (int) result;
    }

    private void drawShineLight(FormattedCharSequence text, float x, float y, Vector4i glowColor, int backgroundColor, BurnishedAuricFontRenderer renderer) {
        renderer.x = x;
        renderer.y = y;
        renderer.endGlow = true;
        renderer.restartBuild();
        renderer.shineColor = glowColor;

        if (isFlashing) glowColor.set(90, 207, 255, 255);
        else glowColor.set(254, 231, 117, 255);

        text.accept(renderer);
        renderer.finish(backgroundColor, x);
    }

    @OnlyIn(Dist.CLIENT)
    private class BurnishedAuricFontRenderer extends BaseFontRenderer {
        private boolean endGlow;
        private Vector4i shineColor;
        private final float glowFactor = (RenderUtil.getLocalTick() + Minecraft.getInstance().getFrameTime()) * -.075f;

        public BurnishedAuricFontRenderer(float x, float y, DisplayMode mode, int color, int light) {
            super(x, y, false, mode, color,  RenderUtil.TRANSIENT_MATRIX, light);
        }

        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
            if (endGlow) {
                float sin = (float) (Math.sin(x * 0.02f + glowFactor) + 1) * 0.5f;
                float glow = (float) Math.pow(sin, 120);
                r = Mth.clamp(shineColor.x / 255f * glow, 0f, 1f);
                g = Mth.clamp(shineColor.y / 255f * glow, 0f, 1f);
                b = Mth.clamp(shineColor.z / 255f * glow, 0f, 1f);
                a = Mth.clamp(shineColor.w / 255f * glow, 0f, 1f);
            }
            return super.accept(positionInCurrentSequence, style, codePoint);
        }
    }
}
