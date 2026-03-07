package hua223.calamity.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import hua223.calamity.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CurseFont extends Font {
    private static final Map<Item, CurseEnchantmentExtensions> EXTENSIONS
        = new Object2ObjectOpenHashMap<>(4);
    private static final Vector4f MIXED_COLOR = RenderUtil.black();
    private static int color;
    private final boolean gradual;
    private final int start;
    private final int end;
    private final int semiCycle;
    private boolean orderGradient = true;
    private int gradualTick;

    private CurseFont(Item item, boolean gradual, int start, int end, int semiCycle) {
        super(Minecraft.getInstance().font.fonts, false);
        this.gradual = gradual;
        this.start = start;
        this.end = end;
        this.semiCycle = semiCycle;
        EXTENSIONS.put(item, new CurseEnchantmentExtensions(this));
    }

    public static CurseFont getOrCreateFont(ItemStack stack) {
        Item item = stack.getItem();
        CurseEnchantmentExtensions extensions = EXTENSIONS.get(item);
        if (extensions != null) return extensions.font();
        else return createDefaultFont(stack);
    }

    private static CurseFont createDefaultFont(ItemStack stack) {
        return new CurseFont(stack.getItem(), false, stack.getRarity().color.getColor(), 0, 0);
    }

    public static void createFont(Item item, boolean gradual, int start, int end, int semiCycle) {
        if (!EXTENSIONS.containsKey(item)) new CurseFont(item, gradual, start, end, semiCycle);
    }

    public static void updateTick() {
        for (CurseEnchantmentExtensions extensions : EXTENSIONS.values())
            extensions.font().updateGradualTick();
    }

    public static CurseEnchantmentExtensions getExtensions(ItemStack stack) {
        Item item = stack.getItem();
        if (EXTENSIONS.containsKey(item)) return EXTENSIONS.get(item);
        createDefaultFont(stack);
        return EXTENSIONS.get(item);
    }

    @Override
    public int drawShadow(PoseStack pose, Component text, float x, float y, int color) {
        reRender(pose, text.getString(), x, y);
        return 0;
    }

    public void reRender(PoseStack stack, String describe, float x, float y) {
        int width = width(describe);
        float backInterpolant = (float) Math.pow(RenderUtil.getLocalTick() * 0.03f % 1f, 1.5f);
        float backScale = Mth.lerp(backInterpolant, 1.0f, 1.2f);

        setMixedColor();
        RenderUtil.interpolateColor(MIXED_COLOR, RenderUtil.DARK_RED, backInterpolant, MIXED_COLOR);
        MIXED_COLOR.mul((float) Math.pow(1f - backInterpolant, 0.46f));
        int mixed = extractMixedColor();

        stack.pushPose();
        stack.translate(x + width / 2f, y + lineHeight / 2f, 0);
        stack.scale(backScale, backScale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        drawShadow(stack, describe, -width / 2f, -lineHeight / 2f, mixed);
        drawShadow(stack, describe, -width / 2f, -lineHeight / 2f, mixed);
        stack.popPose();

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        stack.pushPose();
        stack.translate(0, 0, 0.1f);
        drawShadow(stack, describe, x, y, color);
        RenderSystem.disableBlend();
        stack.popPose();
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

    private void setMixedColor() {
        if (gradual) {
            float progress = (float) (gradualTick < semiCycle ? gradualTick : semiCycle * 2 - gradualTick) / semiCycle;
            color = 255 << 24 | RenderUtil.interpolateColor(start, end, progress);
            MIXED_COLOR.set(RenderUtil.getRed(color), RenderUtil.getGreen(color), RenderUtil.getBlue(color), 255);
            return;
        }

        color = start;
        MIXED_COLOR.set(RenderUtil.getRed(start), RenderUtil.getGreen(start), RenderUtil.getBlue(start), 255);
    }

    private static int extractMixedColor() {
        return ((int) MIXED_COLOR.w() << 24) |
            ((int) MIXED_COLOR.x() << 16) |
            ((int) MIXED_COLOR.y() << 8)  |
            ((int) MIXED_COLOR.z());
    }
}