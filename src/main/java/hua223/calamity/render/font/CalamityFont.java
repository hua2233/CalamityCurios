package hua223.calamity.render.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.mixed.IBakedGlyphAccessor;
import hua223.calamity.register.rune.SpellType;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.EasingType;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

//hua223
@OnlyIn(Dist.CLIENT)
//Refactoring And Standardization
public class CalamityFont extends Font {
    private static short lastRainbowTime;
    private static CalamityFont lastRequester;
    private static int rainbowStyle = 0x00FF0000;
    private static final RenderTarget FONT_TARGET;

    static {
        FONT_TARGET = new TextureTarget(360, 360, false, Minecraft.ON_OSX);
        FONT_TARGET.bindWrite(true);
        FONT_TARGET.setClearColor(0, 0, 0, 0);
    }

    protected CalamityFont() {
        super(Minecraft.getInstance().font.fonts, false);
    }

    protected RenderTarget getFontTarget() {
        FONT_TARGET.clear(Minecraft.ON_OSX);
        FONT_TARGET.bindWrite(false);
        if (lastRequester != this) {
            lastRequester = this;
            this.samplerState();
        }

        return FONT_TARGET;
    }

    protected final float drawBaseTooltipTextAndGlow(FormattedCharSequence text, float x, int backgroundColor, TesselatorRenderer renderer,
                                                    Matrix4f matrix4f,  Vector4i glowColor, int textInnerColor, Vector2f baseOffset) {
        //borderColor
        RenderUtil.multiplyColor(glowColor, 2, glowColor);
        renderer.withColor(FastColor.ARGB32.color(glowColor.w, glowColor.x, glowColor.y, glowColor.z));

        //Draw the base text
        Vector2f offset = new Vector2f();
        for (float f = 0f; f < Mth.TWO_PI; f += Mth.TWO_PI * 0.125f) {
            offset.set(baseOffset);
            offset.rotatedBy(f, Vector2f.ZERO, true);
            //Apply Offset
            renderer.pose.set(matrix4f);
            renderer.pose.translate(offset.x, offset.y, 0);

            renderer.x = x;
            text.accept(renderer);
        }


        renderer.pose.set(matrix4f);
        renderer.withColor(textInnerColor);

        renderer.x = x;
        text.accept(renderer);
        return renderer.finish(backgroundColor, x);
    }

//    //Why was I able to make it so complicated back then，write this kind of thing? Simplification and lightWeighting
//    @Deprecated
//    protected final float drawBaseTooltipTextAndGlow(FormattedCharSequence text, float x, int backgroundColor, TesselatorRenderer renderer,
//                                                     Vector4i glowColor, Vector4i textInnerColor, Vector2f baseOffset) {
//        RenderTarget target = getFontTarget();
//        text.accept(renderer);
//        float result = renderer.finish(backgroundColor, x);
//
//        CalamityShaders.XNA_ALPHA_BLEND.setupRenderState();
//        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
//        RenderSystem.setShaderTexture(0, target.getColorTextureId());
//        RenderUtil.switchScreenRenderState(target);
//
//        float radiusX = target.width / 2f;
//        float radiusY = target.height / 2f;
//        renderer.builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
//
//        //borderColor
//        RenderUtil.multiplyColor(glowColor, 2, glowColor);
//        //Draw the base text
//        Vector2f offset = new Vector2f();
//        for (float f = 0f; f < Mth.TWO_PI; f += Mth.TWO_PI * 0.125f) {
//            offset.set(baseOffset);
//            offset.rotatedBy(f, Vector2f.ZERO, true);
//            //Move To Center And Apply Offset
//            RenderUtil.TRANSIENT_MATRIX.identity().translate(radiusX + offset.x, radiusY + offset.y, 0);
//            renderer.builder.vertex(RenderUtil.TRANSIENT_MATRIX, -radiusX, radiusY, 0).color(glowColor.x, glowColor.y, glowColor.z, glowColor.w).uv(0.0F, 0.0F).endVertex();
//            renderer.builder.vertex(RenderUtil.TRANSIENT_MATRIX, radiusX, radiusY, 0).color(glowColor.x, glowColor.y, glowColor.z, glowColor.w).uv(1F, 0.0F).endVertex();
//            renderer.builder.vertex(RenderUtil.TRANSIENT_MATRIX, radiusX, -radiusY, 0).color(glowColor.x, glowColor.y, glowColor.z, glowColor.w).uv(1F, 1F).endVertex();
//            renderer.builder.vertex(RenderUtil.TRANSIENT_MATRIX, -radiusX, -radiusY, 0).color(glowColor.x, glowColor.y, glowColor.z, glowColor.w).uv(0.0F, 1F).endVertex();
//        }
//
//        //coreColor
//        renderer.builder.vertex(0.0F, target.height, 0.0F).color(textInnerColor.x, textInnerColor.y, textInnerColor.z, textInnerColor.w).uv(0.0F, 0.0F).endVertex();
//        renderer.builder.vertex(target.width, target.height, 0.0F).color(textInnerColor.x, textInnerColor.y, textInnerColor.z, textInnerColor.w).uv(1F, 0.0F).endVertex();
//        renderer.builder.vertex(target.width, 0.0F, 0.0F).color(textInnerColor.x, textInnerColor.y, textInnerColor.z, textInnerColor.w).uv(1F, 1F).endVertex();
//        renderer.builder.vertex(0.0F, 0.0F, 0.0F).color(textInnerColor.x, textInnerColor.y, textInnerColor.z, textInnerColor.w).uv(0.0F, 1F).endVertex();
//        BufferUploader.drawWithShader(renderer.builder.end());
//        RenderUtil.endScreenRenderState();
//        return result;
//    }

    protected void samplerState() {}

    public static int getFormattedCharCount(FormattedCharSequence text) {
        int[] count = new int[] {0};
        text.accept((a, b, c) -> ++count[0] > 0);
        return count[0];
    }

//    public static String getStringFromFormattedChar(FormattedCharSequence text) {
//        CharList list = new CharArrayList();
//
//        text.accept((a, b, c) -> {
//            for (char value : Character.toChars(c)) list.add(value);
//            return true;
//        });
//
//        return String.valueOf(list.toCharArray());
//    }
//
//    public static List<Tuple<String, Style>> getTextFromFormattedChar(FormattedCharSequence text) {
//        List<Tuple<String, Style>> t = new ArrayList<>();
//
//        text.accept((a, b, c) -> {
//            for (char value : Character.toChars(c)) t.add(
//                new Tuple<>(String.valueOf(Character.toChars(value)), b));
//            return true;
//        });
//
//        return t;
//    }

    public static CalamityFont fromTypeSetting(List<ClientTooltipComponent> components, CompoundTag tag, int type) {
        switch (type) {
            case 1 -> {
                String spell = tag.getString("spell");
                if (!spell.isEmpty()) {
                    if (components.get(0) instanceof ClientTextTooltip tooltip)
                        tooltip.text = FormattedCharSequence.composite(SpellType.valueOf(spell).getTypeComponent().append(" ").getVisualOrderText(), tooltip.text);
                    else return null;
                }

                return CurseFont.INSTANCE;
            }

            case 2 -> {
                return BurnishedAuricFont.INSTANCE;
            }

            case 3 -> {
                return NamelessDeityFont.INSTANCE;
            }

            case 4 -> {
                return DemonShadeFont.INSTANCE;
            }

            case 5 -> {
                return DradonTechnologyFont.INSTANCE;
            }

            default -> tag.remove(CalamityHelp.FONT_FLAG);
        }

        return null;
    }

    //tooltip utility method
    private static void rainbowTick() {
        if (lastRainbowTime != RenderUtil.getLocalTick()) {
            lastRainbowTime = RenderUtil.getLocalTick();
            int currentStyle = FastColor.ARGB32.alpha(rainbowStyle);
            //Rotational chromatography
            switch (currentStyle) {
                case 0 -> {
                    int  rainbowG = Math.min(255, FastColor.ARGB32.green(rainbowStyle) + 7) << 8;
                    if (rainbowG >> 8 == 255) {
                        //默认的G分量，其他位信息为0x00
                        rainbowG |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FF00FF;
                    } else rainbowStyle &= 0xFFFF00FF;

                    rainbowStyle |= rainbowG;
                }

                case 1 -> {
                    int rainbowR = Math.max(0, FastColor.ARGB32.red(rainbowStyle) - 7) << 16;
                    if (rainbowR >> 16 == 0) {
                        rainbowR |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x0000FFFF;
                    } else rainbowStyle &= 0xFF00FFFF;

                    rainbowStyle |= rainbowR;
                }

                case 2 -> {
                    int rainbowB = Math.min(255, FastColor.ARGB32.blue(rainbowStyle) + 7);
                    if (rainbowB == 255) {
                        rainbowB |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FFFF00;
                    } else rainbowStyle &= 0xFFFFFF00;

                    rainbowStyle |= rainbowB;
                }

                case 3 -> {
                    int rainbowG = Math.max(0, FastColor.ARGB32.green(rainbowStyle) - 7) << 8;
                    if (rainbowG >> 8 == 0) {
                        rainbowG |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FF00FF;
                    } else rainbowStyle &= 0xFFFF00FF;

                    rainbowStyle |= rainbowG;
                }

                case 4 -> {
                    int rainbowR = Math.min(255, FastColor.ARGB32.red(rainbowStyle) + 7) << 16;
                    if (rainbowR >> 16 == 255) {
                        rainbowR |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x0000FFFF;
                    } else rainbowStyle &= 0xFF00FFFF;

                    rainbowStyle |= rainbowR;
                }

                case 5 -> {
                    int rainbowB = Math.max(0, FastColor.ARGB32.blue(rainbowStyle) - 7);
                    if (rainbowB == 0) rainbowStyle &= 0x00FFFF00;
                    else rainbowStyle &= 0xFFFFFF00;
                    rainbowStyle |= rainbowB;
                }
            }
        }
    }

    public static Component getRainbow(MutableComponent component) {
        return component.setStyle(Style.EMPTY.withColor(getRainbowStyle()));
    }

    public static int getRainbowStyle() {
        rainbowTick();
        //It won't use the Alpha channel
        return rainbowStyle;
    }

    public static Component getAvatar(MutableComponent component) {
        String text = component.getString();
        //Prioritize trying to split along natural spaces.
        int splitLength = text.length() > 1 ? text.length() / 2 : 0;

        if (splitLength != 0) {
            if (text.contains(" ")) {
                int mid = splitLength;
                int right = text.indexOf(' ', mid);
                int left = text.lastIndexOf(' ', mid);
                int spaceIndex;
                if (left == -1) spaceIndex = right;
                else if (right == -1) spaceIndex = left;
                else spaceIndex = (mid - left) <= (right - mid) ? left : right;

                if (spaceIndex != -1) splitLength = spaceIndex;
            }

            String first = text.substring(0, splitLength);
            String second = text.substring(splitLength);
            float colorInterpolant = EasingType.POLY_IN_OUT.easingFunction((float) Math.cos(RenderUtil.getLocalTick() * 0.15f)  * 0.5f + 0.5f, 3);
            component = Component.literal(first).withStyle(Style.EMPTY.withColor(FastColor.ARGB32.lerp(colorInterpolant, 0xFFFF0000, 0xFF69FFFF)));
            component.append(Component.literal(second).withStyle(Style.EMPTY.withColor(FastColor.ARGB32.lerp(1f - colorInterpolant, 0xFFFF0000, 0xFF69FFFF))));
        }

        return component;
    }

    @OnlyIn(Dist.CLIENT)
    protected class BaseFontRenderer extends TesselatorRenderer {
        public BaseFontRenderer(float x, float y, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light) {
            super(x, y, dropShadow, mode, color, pose, light);
        }

        @Override
        public void overwriteDefaultRenderState() {
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            CalamityShaders.XNA_ALPHA_BLEND.setupRenderState();
        }

        @Override
        public void restoreDefaultRenderState() {
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract class TesselatorRenderer implements FormattedCharSink {
        protected float x;
        protected float y;
        protected float r;
        protected float g;
        protected float b;
        protected float a;
        protected Matrix4f pose;
        protected DisplayMode mode;
        protected boolean dropShadow;
        private boolean isDropShadow;
        protected int packedLightCoords;

        @Nullable
        protected List<BakedGlyph.Effect> effects;

        protected RenderType type;
        protected final BufferBuilder builder;

        public TesselatorRenderer(float x, float y, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light) {
            this.x = x;
            this.y = y;
            withColor(color);
            this.pose = pose;
            this.mode = mode;
            this.dropShadow = dropShadow;
            this.packedLightCoords = light;
            builder = RenderSystem.renderThreadTesselator().getBuilder();
            restartBuild();
        }

        public void setDropShadowState(boolean dropShadow) {
            if (this.dropShadow) isDropShadow = dropShadow;
        }

        protected void withColor(int color) {
            r = (color >> 16 & 255) / 255.0F;
            g = (color >> 8 & 255) / 255.0F;
            b = (color & 255) / 255.0F;
            a = (color >> 24 & 255) / 255.0F;
        }

        public void restartBuild() {
            type = null;
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
            FontSet fontset = fonts.apply(style.getFont());
            BakedGlyph glyph = fontset.getGlyph(codePoint);
            GlyphInfo glyphinfo = fontset.getGlyphInfo(codePoint, false);
            BakedGlyph bakedglyph = style.isObfuscated() && codePoint != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(codePoint);
            RenderType characterType = glyph.renderType(DisplayMode.NORMAL);
            if (type != characterType) {
                //When switching between image sets, it is necessary to render immediately, otherwise ..
                if (type != null) {
                    immediatelyRender();
                    restartBuild();
                }

                type = characterType;
            }

            boolean flag = style.isBold();
            TextColor textcolor = getTextColor(style);
            float f;
            float f1;
            float f2;

            if (textcolor != null) {
                int i = textcolor.getValue();
                f = (float)(i >> 16 & 255) / 255.0F;
                f1 = (float)(i >> 8 & 255) / 255.0F;
                f2 = (float)(i & 255) / 255.0F;
            } else {
                f = this.r;
                f1 = this.g;
                f2 = this.b;
            }

            if (isDropShadow) {
                f *= .25f;
                f1 *= .25f;
                f2 *= .25f;
            }

            if (!(glyph instanceof EmptyGlyph)) {
                float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
                float f4 = isDropShadow ? glyphinfo.getShadowOffset() : 0.0F;
                charRender(bakedglyph, flag, style.isItalic(), f5, x + f4, y + f4, pose, builder, f, f1, f2, a, packedLightCoords);
            }

            float f6 = glyphinfo.getAdvance(flag);
            float f7 = isDropShadow ? 1.0F : 0.0F;
            if (style.isStrikethrough()) {
                addEffect(new BakedGlyph.Effect(x + f7 - 1.0F, y + f7 + 4.5F,
                    x + f7 + f6, y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, a));
            }

            if (style.isUnderlined()) {
                addEffect(new BakedGlyph.Effect(x + f7 - 1.0F, y + f7 + 9.0F,
                    x + f7 + f6, y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, a));
            }

            x += f6;
            return true;
        }

        protected TextColor getTextColor(Style style) {
            return null;
        }

        protected void charRender(BakedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y,
                        Matrix4f matrix, VertexConsumer buffer, float red, float green, float blue, float alpha, int packedLight) {
            renderChar(glyph, bold, italic, boldOffset, x, y, matrix, buffer, red, green, blue, alpha, packedLight);
        }

        private void addEffect(BakedGlyph.Effect effect) {
            if (effects == null) effects = new ArrayList<>(4);
            this.effects.add(effect);
        }

        public abstract void overwriteDefaultRenderState();

        public abstract void restoreDefaultRenderState();

        //字体批次整合渲染，在入侵的同时对原版的效果影响最小化
        public float finish(int backgroundColor, float x) {
            if (backgroundColor != 0) {
                float f = (backgroundColor >> 24 & 255) / 255.0F;
                float f1 = (backgroundColor >> 16 & 255) / 255.0F;
                float f2 = (backgroundColor >> 8 & 255) / 255.0F;
                float f3 = (backgroundColor & 255) / 255.0F;
                this.addEffect(new BakedGlyph.Effect(x - 1.0F, y + 9.0F, this.x + 1.0F, y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (effects != null) {
                BakedGlyph bakedglyph = fonts.apply(Style.DEFAULT_FONT).whiteGlyph();
                RenderType t = bakedglyph.renderType(mode);
                if (t != type) {
                    immediatelyRender();
                    restartBuild();
                    type = bakedglyph.renderType(mode);
                }

                for(BakedGlyph.Effect effect : effects)
                    bakedglyph.renderEffect(effect, pose, builder, packedLightCoords);
                effects = null;
            }

            immediatelyRender();
            return this.x;
        }

        public void immediatelyRender() {
            type.setupRenderState();
            overwriteDefaultRenderState();
            BufferUploader.drawWithShader(builder.end());
            type.clearRenderState();
            restoreDefaultRenderState();
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract class RenderTargetRenderer extends TesselatorRenderer {
        protected float targetStep;
        protected boolean splicingStage;
        protected final float charStepSize;
        protected boolean extractCharacterShapes = true;
        protected final RenderTarget target = getFontTarget();

        public RenderTargetRenderer(FormattedCharSequence sequence, boolean dropShadow, DisplayMode mode, int color, Matrix4f pose, int light) {
            super(0, 0, dropShadow, mode, color, pose, light);
            charStepSize = target.width / (float) getFormattedCharCount(sequence);
        }

        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
            splicingStage = true;
            boolean result = super.accept(positionInCurrentSequence, style, codePoint);
            targetStep += charStepSize;
            splicingStage = false;
            return result;
        }

        @Override
        public void restartBuild() {
            if (!splicingStage) targetStep = 0;
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        }

        //Oh, damn it, this densely arranged character set texture can be completely messed up with just a slight offset
        public void extractFontsToTarget(FormattedCharSequence text) {
            RenderSystem.backupProjectionMatrix();
            GlStateManager._viewport(0, 0, target.viewWidth, target.viewHeight);

            PoseStack stack = RenderSystem.getModelViewStack();
            stack.pushPose();
            stack.last().pose().identity().translate(0, 0, -2000);
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setProjectionMatrix(RenderUtil.TRANSIENT_MATRIX.setOrtho(0.0F, target.width,
                target.height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);

            text.accept(this);
            immediatelyRender();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            CalamityShaders.MAIN.bindWrite(true);

            extractCharacterShapes = false;
        }

        @Override
        protected void charRender(BakedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y, Matrix4f matrix,
                                  VertexConsumer buffer, float red, float green, float blue, float alpha, int packedLight) {
            IBakedGlyphAccessor accessor = (IBakedGlyphAccessor) glyph;
            float left = extractCharacterShapes ? targetStep : x + accessor.calamity$GetLeft();
            float right = extractCharacterShapes ? targetStep + charStepSize : x + accessor.calamity$GetRight();
            float upOffset = extractCharacterShapes ? 0 : accessor.calamity$GetUp() - 3.0F;
            float downOffset = extractCharacterShapes ? 0 : accessor.calamity$GetDown() - 3.0F;
            float y0 = extractCharacterShapes ? 0 : y + upOffset;
            float y1 = extractCharacterShapes ? target.height : y + downOffset;

            float u0 = extractCharacterShapes ? accessor.calamity$GetU0() : targetStep / target.width;
            float u1 = extractCharacterShapes ? accessor.calamity$GetU1() : (targetStep + charStepSize) / target.width;
            float v0 = extractCharacterShapes ? accessor.calamity$GetV0() : 0;
            float v1 = extractCharacterShapes ? accessor.calamity$GetV1() : 1;
            if (extractCharacterShapes) {
                buffer.vertex(left, y0, 0.0F).color(red, green, blue, alpha).uv(u0, v0).endVertex();
                buffer.vertex(left, y1, 0.0F).color(red, green, blue, alpha).uv(u0, v1).endVertex();
                buffer.vertex(right, y1, 0.0F).color(red, green, blue, alpha).uv(u1, v1).endVertex();
                buffer.vertex(right, y0, 0.0F).color(red, green, blue, alpha).uv(u1, v0).endVertex();
            } else {
                float leftSlash = italic ? 1.0F - 0.25F * upOffset : 0.0F;
                float rightSlash = italic ? 1.0F - 0.25F * downOffset : 0.0F;

                buffer.vertex(matrix, left + leftSlash, y0, 0.0F).color(red, green, blue, alpha).uv(u0, v1).endVertex();
                buffer.vertex(matrix, left + rightSlash, y1, 0.0F).color(red, green, blue, alpha).uv(u0, v0).endVertex();
                buffer.vertex(matrix, right + rightSlash, y1, 0.0F).color(red, green, blue, alpha).uv(u1, v0).endVertex();
                buffer.vertex(matrix, right + leftSlash, y0, 0.0F).color(red, green, blue, alpha).uv(u1, v1).endVertex();
            }
        }
    }
}
