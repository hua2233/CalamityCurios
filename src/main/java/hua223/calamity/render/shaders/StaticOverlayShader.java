package hua223.calamity.render.shaders;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public class StaticOverlayShader extends CalamityShaders.CalamityShaderInstance {
    private static StaticOverlayShader STATIC_OVERLAY_SHADER;
    private final Uniform RATE;
    private final Uniform ZOOM;

    protected StaticOverlayShader(ResourceProvider provider) throws IOException {
        super(provider, CalamityCurios.ModResource("static_overlay"), DefaultVertexFormat.NEW_ENTITY);
        STATIC_OVERLAY_SHADER = this;
        ZOOM = getUniform("Zoom");
        RATE = getUniform("Rate");
    }

    public static void setZoom(float u, float u1, float v, float v1) {
        STATIC_OVERLAY_SHADER.ZOOM.set(u1, u1 - u, v1, v1 - v);
    }

    public static void setRate(float rate) {
        STATIC_OVERLAY_SHADER.RATE.set(rate);
    }
}
