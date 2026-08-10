package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public interface ICustomBackgroundRender {

    @OnlyIn(Dist.CLIENT)
    RenderType getRenderType();

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    default void render(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo) {
        float[] decomposeGradientColors = {
            FastColor.ARGB32.alpha(colorFrom) / 255.0F,
            FastColor.ARGB32.red(colorFrom) / 255.0F,
            FastColor.ARGB32.green(colorFrom) / 255.0F,
            FastColor.ARGB32.blue(colorFrom) / 255.0F,
            FastColor.ARGB32.alpha(colorTo) / 255.0F,
            FastColor.ARGB32.red(colorTo) / 255.0F,
            FastColor.ARGB32.green(colorTo) / 255.0F,
            FastColor.ARGB32.blue(colorTo) / 255.0F
        };

        internalRender(guiGraphics.bufferSource().getBuffer(getRenderType()),
            x1, y1, x2, y2, z, decomposeGradientColors, guiGraphics.pose().last().pose());
        guiGraphics.flushIfUnmanaged();
    }

    @OnlyIn(Dist.CLIENT)
    void internalRender(VertexConsumer consumer, int x1, int y1, int x2, int y2, int z, float[] gradientColor, Matrix4f matrix4f);
}
