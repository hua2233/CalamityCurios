package hua223.calamity.render.primitive;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public abstract class PrimitiveSettings {
    public final RenderType shader;
    protected VertexArgumentWrapper[] wrappersBuffer;
    private short index;

    public PrimitiveSettings(RenderType shader) {
        this.shader = shader;
    }

    public abstract float vertexWidth(float completionRatio);

    public abstract Vector4i vertexColor(float completionRatio);

    public abstract int getCapacity();

    public abstract float widthCorrectionRatio();

    public abstract VertexConsumer getConsumer();

    public void offset(float completionRatio, Vector2f vertex) {}

    public void addArgument(Vector2f position, Vector4i color, Vector2f uv, float widthCorrectionFactor) {
        VertexArgumentWrapper wrapper = wrappersBuffer[index];
        if (wrapper == null)
            wrappersBuffer[index] = new VertexArgumentWrapper(position, color, uv, widthCorrectionFactor);
        else wrapper.reLoad(position, color, uv, widthCorrectionFactor);
        index++;
    }

    public boolean smoothen() {
        return true;
    }

    protected void buildVertex(VertexConsumer consumer, Matrix4f matrix4f, short index) {
        VertexArgumentWrapper vertex = wrappersBuffer[index];

        consumer.vertex(matrix4f, vertex.position.x, vertex.position.y, 0)
            .color(vertex.r, vertex.g, vertex.b, vertex.a)
            .uv(vertex.uv.x, vertex.uv.y)
            .uv2(vertex.w)
            .endVertex();
    }

    public void initVertexArgumentBuffer(int size) {
        index = 0;
        if (wrappersBuffer == null) wrappersBuffer = new VertexArgumentWrapper[size * getCapacity()];
    }
}
