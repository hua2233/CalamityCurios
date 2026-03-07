package hua223.calamity.util.primitive;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class PrimitiveSettings {
    public final Function<Float, Float> vertexWidthFunction;
    public final Function<Float, Vector4f> vertexColorFunction;
    public final int widthCorrectionRatio;
    public final boolean smoothen;
    public final RenderType shader;
    private final int capacity;
    public MultiBufferSource source;
    VertexArgumentWrapper[] wrappersBuffer;
    private boolean bufferInit;
    private short index;


    public PrimitiveSettings(Function<Float, Float> vertexWidthFunction,
                             Function<Float, Vector4f> vertexColorFunction,
                             boolean smoothen, int widthCorrectionRatio,
                             int capacity, RenderType shader) {
        this.vertexColorFunction = vertexColorFunction;
        this.vertexWidthFunction = vertexWidthFunction;
        this.widthCorrectionRatio = widthCorrectionRatio;
        this.smoothen = smoothen;
        this.shader = shader;
        this.capacity = capacity;
    }

    public void addArgument(Vec3 position, Vector4f color, Vec2 uv, float widthCorrectionFactor) {
        VertexArgumentWrapper wrapper = wrappersBuffer[index];
        if (wrapper == null)
            wrappersBuffer[index] = new VertexArgumentWrapper(position, color, uv, widthCorrectionFactor);
        else wrapper.reLoad(position, color, uv, widthCorrectionFactor);
        index++;
    }

    public PrimitiveSettings setBufferSource(MultiBufferSource source) {
        this.source = source;
        return this;
    }

    public void initVertexArgumentBuffer(int size) {
        if (!bufferInit) {
            wrappersBuffer = new VertexArgumentWrapper[size * capacity];
            bufferInit = true;
        }

        index = 0;
    }

    public VertexConsumer getConsumer() {
        return source.getBuffer(shader);
    }
}
