package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.RenderUtil;
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
    default void render(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int z, int colorForm, int colorTo) {
        internalRender(guiGraphics.bufferSource().getBuffer(getRenderType()), x1, y1, x2, y2, z,
            colorForm, colorTo, RenderUtil.TRANSIENT_MATRIX.set(guiGraphics.pose().last().pose()));
        guiGraphics.flushIfUnmanaged();
    }

    @OnlyIn(Dist.CLIENT)
    default void internalRender(VertexConsumer consumer, int x1, int y1, int x2, int y2, int z, int colorForm, int colorTo, Matrix4f matrix4f) {
        float[] gradientColors = new float[] {
            FastColor.ARGB32.alpha(colorForm) / 255.0F,
            FastColor.ARGB32.red(colorForm) / 255.0F,
            FastColor.ARGB32.green(colorForm) / 255.0F,
            FastColor.ARGB32.blue(colorForm) / 255.0F,

            FastColor.ARGB32.alpha(colorTo) / 255.0F,
            FastColor.ARGB32.red(colorTo) / 255.0F,
            FastColor.ARGB32.green(colorTo) / 255.0F,
            FastColor.ARGB32.blue(colorTo) / 255.0F
        };

        consumer.vertex(matrix4f, x1, y1, z)
            .color(gradientColors[1], gradientColors[2], gradientColors[3], gradientColors[0])
            .uv(1f, 1f)
            .endVertex();

        consumer.vertex(matrix4f, x1, y2, z)
            .color(gradientColors[5], gradientColors[6], gradientColors[7], gradientColors[4])
            .uv(1f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y2, z)
            .color(gradientColors[5], gradientColors[6], gradientColors[7], gradientColors[4])
            .uv(0f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y1, z)
            .color(gradientColors[1], gradientColors[2], gradientColors[3], gradientColors[0])
            .uv(0f, 1f)
            .endVertex();
    }
}
