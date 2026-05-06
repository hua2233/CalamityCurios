package hua223.calamity.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import hua223.calamity.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CurseFont extends Font {
    private static final Map<Item, CurseEnchantmentExtensions> EXTENSIONS
        = new Object2ObjectOpenHashMap<>(4);
    //This type no longer mounts formatted data onto font objects to avoid creating the same font multiple times
    public static final CurseFont INSTANCE = new CurseFont();
    private final Matrix4f TRANSIENT_MATRIX = new Matrix4f();
    private final Vector4i MIXED_COLOR = RenderUtil.black();
    private int color;
    private FontFormat format;

    private CurseFont() {
        super(Minecraft.getInstance().font.fonts, false);
    }

    public CurseFont setRenderFormat(FontFormat format) {
        this.format = format;
        return this;
    }

    public static CurseFont setOrCreateFormat(ItemStack stack) {
        Item item = stack.getItem();
        CurseEnchantmentExtensions extensions = EXTENSIONS.get(item);
        if (extensions != null) return INSTANCE.setRenderFormat(extensions.format());
        else return createDefaultFontFormat(stack);
    }

    public static void reSet() {
        EXTENSIONS.clear();
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    private static CurseFont createDefaultFontFormat(ItemStack stack) {
        var extensions = new CurseEnchantmentExtensions(new FontFormat(
            false, stack.getRarity().color.getColor(), 0, 0));
        EXTENSIONS.put(stack.getItem(), extensions);
        return INSTANCE.setRenderFormat(extensions.format());
    }

    public static void createFont(Item item, boolean gradual, int start, int end, int semiCycle) {
        EXTENSIONS.computeIfAbsent(item, key -> new CurseEnchantmentExtensions(new FontFormat(gradual, start, end, semiCycle)));
    }

    public static void updateTick() {
        for (CurseEnchantmentExtensions extensions : EXTENSIONS.values())
            extensions.format().updateGradualTick();
    }

    public static CurseEnchantmentExtensions getExtensions(ItemStack stack) {
        Item item = stack.getItem();
        if (EXTENSIONS.containsKey(item)) return EXTENSIONS.get(item);
        createDefaultFontFormat(stack);
        return EXTENSIONS.get(item);
    }

    //重写以重定向Gui调用
    @Override
    public int drawInBatch(@NotNull FormattedCharSequence text, float x, float y, int color, boolean dropShadow, @NotNull Matrix4f matrix,
                           @NotNull MultiBufferSource buffer, @NotNull DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
        int width = preRender(width(text), matrix, x, y);
        int mixed = extractMixedColor();

        super.drawInBatch(text, -width / 2f, -lineHeight / 2f, mixed, true,
            TRANSIENT_MATRIX, buffer, displayMode, backgroundColor, packedLightCoords);

        super.drawInBatch(text, -width / 2f, -lineHeight / 2f, mixed, true,
            TRANSIENT_MATRIX, buffer, displayMode, backgroundColor, packedLightCoords);

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        matrix.translate(0, 0, 0.1f);
        int i = super.drawInBatch(text, x, y, this.color, true,
            matrix, buffer, displayMode, backgroundColor, packedLightCoords);
        RenderSystem.disableBlend();
        return i;
    }

    private int preRender(int width, Matrix4f sourceMatrix, float x, float y) {
        float backInterpolant = (float) Math.pow(RenderUtil.getLocalTick() * 0.03f % 1f, 1.5f);
        float backScale = Mth.lerp(backInterpolant, 1.0f, 1.2f);

        setMixedColor();
        RenderUtil.multiplyColor(RenderUtil.interpolateColor(MIXED_COLOR, RenderUtil.DARK_RED, backInterpolant, MIXED_COLOR),
            (float) Math.pow(1f - backInterpolant, 0.46f), MIXED_COLOR);

        TRANSIENT_MATRIX.set(sourceMatrix);
        TRANSIENT_MATRIX.translate(x + width / 2f, y + lineHeight / 2f, 0);
        TRANSIENT_MATRIX.scale(backScale, backScale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        return width;
    }

    //供给给CurseTooltipExtensions调用
    public void reRender(Matrix4f matrix4f, String describe, float x, float y, MultiBufferSource source) {
        int width = preRender(width(describe), matrix4f, x, y);
        int mixed = extractMixedColor();

        super.drawInBatch(describe, -width / 2f, -lineHeight / 2f, mixed, true,
            TRANSIENT_MATRIX, source, DisplayMode.NORMAL, 0, 15728880);

        super.drawInBatch(describe, -width / 2f, -lineHeight / 2f, mixed, true,
            TRANSIENT_MATRIX, source, DisplayMode.NORMAL, 0, 15728880);

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        matrix4f.translate(0, 0, 0.1f);
        super.drawInBatch(describe, x, y, color, true,
            matrix4f, source, DisplayMode.NORMAL, 0, 15728880);
        RenderSystem.disableBlend();
    }



    private void setMixedColor() {
        if (format.gradual) {
            float progress = (float) (format.gradualTick < format.semiCycle ? format.gradualTick :format. semiCycle * 2 - format.gradualTick) / format.semiCycle;
            color = 255 << 24 | FastColor.ARGB32.lerp(progress, format.start, format.end);
            MIXED_COLOR.set(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), 255);
            return;
        }

        color = format.start;
        MIXED_COLOR.set(FastColor.ARGB32.red(format.start), FastColor.ARGB32.green(format.start), FastColor.ARGB32.blue(format.start), 255);
    }

    private int extractMixedColor() {
        return (MIXED_COLOR.w() << 24) |
            (MIXED_COLOR.x() << 16) |
            (MIXED_COLOR.y() << 8)  |
            MIXED_COLOR.z();
    }

    public static class FontFormat {
        private final boolean gradual;
        private final int start;
        private final int end;
        private final int semiCycle;
        private boolean orderGradient = true;
        private int gradualTick;

        FontFormat(boolean gradual, int start, int end, int semiCycle) {
            this.gradual = gradual;
            this.start = start;
            this.end = end;
            this.semiCycle = semiCycle;
        }

        private void updateGradualTick() {
            if (gradual) {
                if (orderGradient) {
                    gradualTick++;
                    if (gradualTick >= semiCycle) orderGradient = false;
                } else {
                    gradualTick--;
                    if (gradualTick <= 0) orderGradient = true;
                }
            }
        }
    }
}