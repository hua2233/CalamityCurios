package hua223.calamity.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.screen.particleset.CodeSymbolsSet;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class DradonTechnologyFont extends CalamityFont {
    private static TextureAtlasSprite CODE_SYMBOLS;
    public static final DradonTechnologyFont INSTANCE = new DradonTechnologyFont();

    private short tick;
    private CodeSymbolsSet set;

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        int outerColor = RenderUtil.colorSwap(0xFF75E2D3, 0xFF9BB9CD,3);
        outerColor = FastColor.ARGB32.lerp(0.6f, outerColor, 0xFF008B8B);

        Vector4i outer = new Vector4i(FastColor.ARGB32.red(outerColor), FastColor.ARGB32.green(outerColor), FastColor.ARGB32.blue(outerColor), 255);

        int textInnerColor = FastColor.ARGB32.lerp(.2f, 0xFF000000, outerColor);
        textInnerColor = FastColor.ARGB32.lerp(0.9f, 0xFF778899, textInnerColor) | 0xFF000000;

        //Base Mixed Color
        RenderSystem.setShaderColor(101 / 255f, 115 / 255f, 130 / 255f, 216 / 255f);
        BaseFontRenderer renderer = new BaseFontRenderer(x, y, false, displayMode, 0xFFFFFFFF, RenderUtil.TRANSIENT_MATRIX, packedLightCoords);
        int i = (int) drawBaseTooltipTextAndGlow(text, x, backgroundColor, renderer, matrix, outer, textInnerColor, new Vector2f(0.48f, 0f));
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        drawCodeSymbols(x, y, matrix, text);
        return i;
    }

//    @Override
//    public void reRender(Matrix4f matrix4f, String describe, float x, float y, MultiBufferSource source) {
//        int outerColor = RenderUtil.colorSwap(0xFF75E2D3, 0xFF9BB9CD,3);
//        outerColor = FastColor.ARGB32.lerp(0.6f, outerColor, 0xFF008B8B);
//
//        Vector4i outer = new Vector4i(FastColor.ARGB32.red(outerColor), FastColor.ARGB32.green(outerColor), FastColor.ARGB32.blue(outerColor), 255);
//
//        int textInnerColor = FastColor.ARGB32.lerp(.2f, 0xFF000000, outerColor);
//        textInnerColor = FastColor.ARGB32.lerp(0.9f, 0xFF778899, textInnerColor);
//
//        Vector4i textInner = new Vector4i(FastColor.ARGB32.red(textInnerColor), FastColor.ARGB32.green(textInnerColor), FastColor.ARGB32.blue(textInnerColor), 255);
//
//        drawBaseTooltipTextAndGlow(describe, x, y, 0, new BaseFontRenderer(x, y, false,
//            DisplayMode.NORMAL, 0xD8657382, matrix4f, LightTexture.FULL_BRIGHT), outer, textInner, new Vector2f(2.4f, 0f));
//
//        drawCodeSymbols(x, y, matrix4f, describe);
//    }

    private void drawCodeSymbols(float x, float y, Matrix4f matrix4f, FormattedCharSequence text) {
        if (set == null) {
            set = new CodeSymbolsSet(random, lineHeight);
            Minecraft minecraft = Minecraft.getInstance();
            //Clear Task Setup
            DelayRunnable.addUniqueLoopTask(() -> {
                //如果游戏结束或者已经不在GUI界面查看物品Tooltip就释放资源
                if (minecraft.level == null || minecraft.screen == null) {
                    set = null;
                    return true;
                }

                return false;
            }, 30, DradonTechnologyFont.class);
        }

        if (tick != RenderUtil.getLocalTick()) {
            tick = RenderUtil.getLocalTick();
            set.update();
        }

        set.drawSet(x, y, width(text), matrix4f, CODE_SYMBOLS);
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        CODE_SYMBOLS = atlas.getSprite(CalamityCurios.ModResource("code_symbol"));
    }
}
