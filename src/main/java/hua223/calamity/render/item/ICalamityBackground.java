package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public interface ICalamityBackground extends ICustomBackgroundRender {
    int[] CALAMITY_COLOR = new int[] {
        238, 58, 58, //GriefColor
        33, 158, 248, //LamentColor
        255, 255, 143, //EpiphanyColor
        78, 78, 78, //AcceptanceColor
        0, 0 //Time records and stages
    };

    @Override
    default void internalRender(VertexConsumer consumer, int x1, int y1, int x2, int y2, int z, int colorForm, int colorTo, Matrix4f matrix4f) {
        int stages = CALAMITY_COLOR[13];
        int r = CALAMITY_COLOR[stages];
        int g = CALAMITY_COLOR[++stages];
        int b = CALAMITY_COLOR[++stages];
        int periodicValue = RenderUtil.processingCycleTime(CALAMITY_COLOR[12]);
        if (periodicValue > 170) {
            float fadeIn = (periodicValue - 170 + Minecraft.getInstance().getPartialTick()) / 30f;
            if (fadeIn >= 1f) {
                fadeIn = 1f;
                CALAMITY_COLOR[12] = RenderUtil.getLocalTick();
                CALAMITY_COLOR[13] = (CALAMITY_COLOR[13] + 3) % 12;
            }

            r = Mth.lerpInt(fadeIn, r, CALAMITY_COLOR[++stages % 12]);
            g = Mth.lerpInt(fadeIn, g, CALAMITY_COLOR[++stages % 12]);
            b = Mth.lerpInt(fadeIn, b, CALAMITY_COLOR[++stages % 12]);
        }

        consumer.vertex(matrix4f, x1, y1, z)
            .color(r, g, b, 255)
            .uv(1f, 1f)
            .endVertex();

        consumer.vertex(matrix4f, x1, y2, z)
            .color(r, g, b, 255)
            .uv(1f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y2, z)
            .color(r, g, b, 255)
            .uv(0f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y1, z)
            .color(r, g, b, 255)
            .uv(0f, 1f)
            .endVertex();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default RenderType getRenderType() {
        return CalamityShaders.getCalamityBG();
    }
}
