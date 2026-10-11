package hua223.calamity.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class DemonShadeFont extends CalamityFont {
    public static final DemonShadeFont INSTANCE = new DemonShadeFont();

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        DemonShadeRenderer renderer = new DemonShadeRenderer(text, false, displayMode, 0xFFFFFFFF, matrix, packedLightCoords);
        renderer.extractFontsToTarget(text);

        renderer.withColor(0xE5000000);
        //Get an offset to the afterimageOffset based on a sine wave.
        Vector2f afterimageOffset = new Vector2f();
        float sine = (float)((1f + Math.sin((RenderUtil.getLocalTick() + Minecraft.getInstance().getPartialTick()) * .01f)) * 0.5f);
        float sineOffset = Mth.lerp(sine, 0.68f, 0.717f);

        //Draw text backGlow effects.
        for (int i = 0; i < 12; i++) {
            renderer.restartBuild();
            float f  = Mth.TWO_PI * i / 12f;
            Vector2f.toRotationVector2(f, afterimageOffset);
            // Draw the text. Rotate the position based on i.
            afterimageOffset.mul(sineOffset).rotatedBy(f, Vector2f.ZERO, true).add(x, y);
            renderer.x = afterimageOffset.x;
            renderer.y = afterimageOffset.y;
            text.accept(renderer);
            renderer.immediatelyRender();
        }

        //Draw the main inner text.
        renderer.x = x;
        renderer.y = y;
        renderer.restartBuild();
        renderer.withColor(0xFF6B65B4);
        text.accept(renderer);
        return (int) renderer.finish(backgroundColor, x);
    }

    @Override
    protected void samplerState() {
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
    }

    @OnlyIn(Dist.CLIENT)
    private class DemonShadeRenderer extends RenderTargetRenderer {
        public DemonShadeRenderer(FormattedCharSequence sequence, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light) {
            super(sequence, dropShadow, mode, color, pose, light);
        }

        @Override
        public void overwriteDefaultRenderState() {
            if (extractCharacterShapes) RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            else {
                RenderSystem.enableBlend();
                CalamityShaders.XNA_ALPHA_BLEND.setupRenderState();
                Supplier<ShaderInstance> supplier = CalamityShaders.getDemonShadeFont();
                supplier.get().safeGetUniform("CharStep").set(charStepSize / target.width);
                RenderSystem.setShader(supplier);
                RenderSystem.setShaderTexture(0, target.getColorTextureId());
                RenderSystem.setShaderTexture(1, CalamityShaders.PERLIN2);
            }
        }

        @Override
        public void restoreDefaultRenderState() {
            RenderSystem.disableBlend();
        }
    }
}
