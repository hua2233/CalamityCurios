package hua223.calamity.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class NamelessDeityFont extends CalamityFont {
    public static final NamelessDeityFont INSTANCE = new NamelessDeityFont();

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        NamelessDeityRenderer renderer = new NamelessDeityRenderer(text, dropShadow, displayMode, 0xFFFFFFFF, matrix, packedLightCoords);
        renderer.extractFontsToTarget(text);

        renderer.x = x;
        renderer.y = y;
        renderer.restartBuild();
        text.accept(renderer);
        return (int) renderer.finish(backgroundColor, x);
    }

    @Override
    public void samplerState() {
        //LinearClamp
        GlStateManager._texParameter(3553, 10241, 9729);
        GlStateManager._texParameter(3553, 10240, 9729);
        GlStateManager._texParameter(3553, 34892, 0);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
    }

    @OnlyIn(Dist.CLIENT)
    protected class NamelessDeityRenderer extends RenderTargetRenderer {
        public NamelessDeityRenderer(FormattedCharSequence sequence, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light) {
            super(sequence, dropShadow, mode, color, pose, light);
        }

        @Override
        public void overwriteDefaultRenderState() {
            if (extractCharacterShapes) RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            else {
                RenderSystem.enableBlend();
                CalamityShaders.XNA_ALPHA_BLEND.setupRenderState();
                Supplier<ShaderInstance> supplier = CalamityShaders.getNamelessDeityFontShader();

                supplier.get().safeGetUniform("ChromaticAberrationOffset").set(RenderUtil.cos01(
                    (RenderUtil.getLocalTick() + Minecraft.getInstance().getPartialTick()) * 0.006f) * 8f);

                RenderSystem.setShader(supplier);
                RenderSystem.setShaderTexture(0, target.getColorTextureId());
                RenderSystem.setShaderTexture(1, CalamityShaders.PERLIN);
            }
        }

        @Override
        public void restoreDefaultRenderState() {
            RenderSystem.disableBlend();
        }
    }
}
