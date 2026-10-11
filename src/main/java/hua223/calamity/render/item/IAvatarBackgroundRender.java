package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.render.shaders.CalamityShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public interface IAvatarBackgroundRender extends ICustomBackgroundRender {
    @Override
    @OnlyIn(Dist.CLIENT)
    default RenderType getRenderType() {
        return CalamityShaders.getSuperCosmic();
    }

    @OnlyIn(Dist.CLIENT)
    default float getTileScale() {
        return 256f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default void internalRender(VertexConsumer consumer, int x1, int y1, int x2, int y2, int z, int colorForm, int colorTo, Matrix4f matrix4f) {
        float tileSize = getTileScale();
        float u = (x2 - x1) / tileSize;
        float v = (y2 - y1) / tileSize;

        consumer.vertex(matrix4f, x1, y1, z)
            .uv(u, v)
            .endVertex();

        consumer.vertex(matrix4f, x1, y2, z)
            .uv(u, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y2, z)
            .uv(0f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y1, z)
            .uv(0f, v)
            .endVertex();
    }
}
