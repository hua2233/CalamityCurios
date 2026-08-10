package hua223.calamity.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.render.CurseEnchantmentExtensions;
import net.minecraft.client.player.LocalPlayer;
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
public class CurseFont extends Font implements ICalamityFont {
    private static final Map<Item, CurseEnchantmentExtensions> EXTENSIONS
        = new Object2ObjectOpenHashMap<>(4);
    //This type no longer mounts formatted data onto font objects to avoid creating the same font multiple times
    public static final CurseFont INSTANCE = new CurseFont();
    private final Vector4i MIXED_COLOR = RenderUtil.black();
    private int color;
    private FontFormat format;

    private CurseFont() {
        super(Minecraft.getInstance().font.fonts, false);
    }

    public CurseFont setRenderFormat(FontFormat format) {
        this.format = format.updateTick();
        return this;
    }

    public static CurseFont setOrCreateFormat(ItemStack stack) {
        Item item = stack.getItem();
        CurseEnchantmentExtensions extensions = EXTENSIONS.get(item);
        if (extensions != null) return INSTANCE.setRenderFormat(extensions.format());
        else return createDefaultFontFormat(stack);
    }

    @LogoutRelease
    public static void reSet(LocalPlayer player) {
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
        Matrix4f transientMatrix = RenderUtil.TRANSIENT_MATRIX;
        int width = preRender(width(text), matrix, transientMatrix, x, y);

        super.drawInBatch(text, -width / 2f, -lineHeight / 2f, -1, true,
            transientMatrix, buffer, displayMode, backgroundColor, packedLightCoords);

        super.drawInBatch(text, -width / 2f, -lineHeight / 2f, -1, true,
            transientMatrix, buffer, displayMode, backgroundColor, packedLightCoords);

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        matrix.translate(0, 0, 0.1f);
        int i = super.drawInBatch(text, x, y, this.color, true,
            matrix, buffer, displayMode, backgroundColor, packedLightCoords);
        RenderSystem.disableBlend();
        return i;
    }

    private int preRender(int width, Matrix4f sourceMatrix, Matrix4f transientMatrix, float x, float y) {
        float backInterpolant = (float) Math.pow(RenderUtil.getLocalTick() * 0.03f % 1f, 1.5f);
        float backScale = Mth.lerp(backInterpolant, 1.0f, 1.2f);

        setMixedColor();
        RenderUtil.multiplyColor(RenderUtil.interpolateColor(MIXED_COLOR, new Vector4i(139, 0, 0, 255), backInterpolant, MIXED_COLOR),
            (float) Math.pow(1f - backInterpolant, 0.46f), MIXED_COLOR);

        transientMatrix.set(sourceMatrix);
        transientMatrix.translate(x + width / 2f, y + lineHeight / 2f, 0);
        transientMatrix.scale(backScale, backScale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        return width;
    }

    @Override
    public void reRender(Matrix4f matrix4f, String describe, float x, float y, MultiBufferSource source) {
        Matrix4f transientMatrix = RenderUtil.TRANSIENT_MATRIX;
        int width = preRender(width(describe), matrix4f, transientMatrix, x, y);
        int mixed = extractMixedColor();

        super.drawInBatch(describe, -width / 2f, -lineHeight / 2f, mixed, true,
            transientMatrix, source, DisplayMode.NORMAL, 0, 15728880);

        super.drawInBatch(describe, -width / 2f, -lineHeight / 2f, mixed, true,
            transientMatrix, source, DisplayMode.NORMAL, 0, 15728880);

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        matrix4f.translate(0, 0, 0.1f);
        super.drawInBatch(describe, x, y, color, true,
            matrix4f, source, DisplayMode.NORMAL, 0, 15728880);
        RenderSystem.disableBlend();
    }

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    @Override
    public int getWidth(String text) {
        return width(text);
    }

    private void setMixedColor() {
        if (format.gradual) {
            float progress = (float) (format.gradualTick < format.semiCycle ? format.gradualTick : format.semiCycle * 2 - format.gradualTick) / format.semiCycle;
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
        private int lastTime;
        private boolean orderGradient = true;
        private int gradualTick;

        FontFormat(boolean gradual, int start, int end, int semiCycle) {
            this.gradual = gradual;
            this.start = start;
            this.end = end;
            this.semiCycle = semiCycle;
        }

        private FontFormat updateTick() {
            int interval =  Math.abs(RenderUtil.getLocalTick() - lastTime);
            if (interval > 0) {
                if (interval > 10) {
                    orderGradient = true;
                    gradualTick = 0;
                }

                if (gradual) {
                    if (orderGradient) {
                        gradualTick++;
                        if (gradualTick >= semiCycle) orderGradient = false;
                    } else {
                        gradualTick--;
                        if (gradualTick <= 0) orderGradient = true;
                    }
                }

                lastTime = RenderUtil.getLocalTick();
            }

            return this;
        }
    }
}