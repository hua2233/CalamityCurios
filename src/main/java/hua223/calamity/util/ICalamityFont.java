package hua223.calamity.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;

@OnlyIn(Dist.CLIENT)
public interface ICalamityFont {
    //供给给CalamityTooltipExtensions调用
    void reRender(Matrix4f matrix4f, String describe, float x, float y, MultiBufferSource source);

    int getLineHeight();

    int getWidth(String text);

    default void drawMultiShadow(String text, float x, float y, int color, Matrix4f matrix,
                            MultiBufferSource buffer, Font.DisplayMode displayMode) {
        StringDecomposer.iterateFormatted(text, Style.EMPTY, new ShadowString((Font) this, buffer, matrix,
            displayMode, getShadowDirections(), new float[] {x, y}, decomposeColor(color)));
    }

    default Vector2f[] getShadowDirections() {
        return new Vector2f[] {
            new Vector2f(-0.8f, 0f), new Vector2f(0.8f, 0f),
            new Vector2f(0f, -0.8f), new Vector2f(0f, 0.8f)
        };
    }

    default float[] decomposeColor(int textColor) {
        return new float[] {
            FastColor.ARGB32.red(textColor) / 255F,
            FastColor.ARGB32.green(textColor) / 255F,
            FastColor.ARGB32.blue(textColor) / 255F,
            FastColor.ARGB32.alpha(textColor) / 255F
        };
    }

    record ShadowString(Font font, MultiBufferSource source, Matrix4f matrix4f,
                        Font.DisplayMode displayMode, Vector2f[] shadowDirections,
                        float[] pos, float[] color) implements FormattedCharSink {
        @Override
        public boolean accept(int i, @NotNull Style style, int codePoint) {
            FontSet fontset = font.fonts.apply(style.getFont());
            BakedGlyph glyph = fontset.getGlyph(codePoint);
            RenderType type = glyph.renderType(displayMode);

            for (Vector2f shadowDirection : shadowDirections) {
                VertexConsumer vertexconsumer = source.getBuffer(type);
                font.renderChar(glyph, style.isBold(), style.isItalic(), 0, pos[0] + shadowDirection.x, pos[1]
                    + shadowDirection.y, matrix4f, vertexconsumer, color[0], color[1], color[2], color[3], 15728880);
            }

            pos[0] += 9;
            return true;
        }
    }
}
