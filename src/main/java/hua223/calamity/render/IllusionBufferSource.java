package hua223.calamity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.events.LogoutRelease;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class IllusionBufferSource implements MultiBufferSource {
    private static IllusionBufferSource source;
    private static short reference;
    private MultiBufferSource hijackSource;
    private final IllusionVertexConsumer vertexConsumer;

    private IllusionBufferSource() {
        vertexConsumer = new IllusionVertexConsumer();
    }

    public static @NotNull MultiBufferSource getSource(MultiBufferSource source) {
        IllusionBufferSource.source.hijackSource = source ==
            IllusionBufferSource.source ? IllusionBufferSource.source.hijackSource : source;
        return IllusionBufferSource.source;
    }

    public static void create() {
        if (source == null) source = new IllusionBufferSource();
        reference++;
    }

    public static void setColor(int r, int g, int b, int a) {
        source.vertexConsumer.defaultColor(r, g, b, a);
    }

    public static void setColor(float r, float g, float b, float a) {
        source.vertexConsumer.defaultColor(r, g, b, a);
    }

    @LogoutRelease
    public static void onLogOut(LocalPlayer player) {
        reference = 0;
        source = null;
    }

    public static void destroy() {
        if (reference - 1 == 0) {
            reference--;
            source = null;
        }
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        return vertexConsumer.setDelegate(hijackSource.getBuffer(renderType));
    }

    @OnlyIn(Dist.CLIENT)
    public static class IllusionVertexConsumer implements VertexConsumer {
        private VertexConsumer hijackConsumer;
        private float r;
        private float g;
        private float b;
        private float a;

        IllusionVertexConsumer() {}

        public VertexConsumer setDelegate(VertexConsumer consumer) {
            hijackConsumer = consumer;
            return this;
        }

        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            hijackConsumer = hijackConsumer.vertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer vertex(@NotNull Matrix4f matrix, float x, float y, float z) {
            hijackConsumer = hijackConsumer.vertex(matrix, x, y, z);
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, float r, float g, float b, float a, float texU,
                           float texV, int overlay, int light, float normalX, float normalY, float normalZ) {
            hijackConsumer.vertex(x, y, z, this.r, this.g, this.b, this.a, texU, texV, overlay, light, normalX, normalY, normalZ);
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            defaultColor(r / 255f, g / 255f, b / 255f, a / 255f);
        }

        public void defaultColor(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public void putBulkData(PoseStack.@NotNull Pose entry, @NotNull BakedQuad quad, float red, float green, float blue, int light, int overlay) {
            hijackConsumer.putBulkData(entry, quad, red, green, blue, light, overlay);
        }

        @Override
        public void putBulkData(PoseStack.@NotNull Pose entry, @NotNull BakedQuad quad, float @NotNull [] colorMuls,
                                float red, float green, float blue, int @NotNull [] lights, int overlay, boolean mulColor) {
            hijackConsumer.putBulkData(entry, quad, colorMuls, red, green, blue, lights, overlay, mulColor);
        }

        @Override
        public void putBulkData(PoseStack.@NotNull Pose pose, @NotNull BakedQuad bakedQuad, float red, float green,
                                float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
            hijackConsumer.putBulkData(pose, bakedQuad, r > 0 ? r : red, g > 0 ? g : green,
                b > 0 ? b : blue, a > 0 ? a : alpha, packedLight, packedOverlay, readExistingColor);
        }

        @Override
        public void putBulkData(PoseStack.@NotNull Pose pose, @NotNull BakedQuad quad, float @NotNull [] vs, float r,
                                float g, float b, float a, int @NotNull [] uv, int light, boolean mulColor) {
            hijackConsumer.putBulkData(pose, quad, vs, this.r, this.g, this.b, this.a, uv, light, mulColor);
        }

        @Override
        public @NotNull VertexConsumer color(int i, int i1, int i2, int i3) {
            //End proxy immediately after proxy color
            return hijackConsumer.color(r * 255, g * 255, b * 255, a * 255);
        }

        @Override
        public @NotNull VertexConsumer uv(float v, float v1) {
            hijackConsumer.uv(v, v1);
            return this;
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int i, int i1) {
            hijackConsumer.overlayCoords(i, i1);
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv2(int i, int i1) {
            hijackConsumer.uv2(i, i1);
            return this;
        }

        @Override
        public @NotNull VertexConsumer normal(float v, float v1, float v2) {
            hijackConsumer.normal(v, v1, v2);
            return this;
        }

        @Override
        public void endVertex() {
            hijackConsumer.endVertex();
        }

        @Override
        public @NotNull VertexConsumer color(float red, float green, float blue, float alpha) {
            return hijackConsumer.color(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {}
    }
}
