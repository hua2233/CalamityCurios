package hua223.calamity.render.shaders;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FastColor;

import java.io.IOException;

public class DraedonBarrierRenderer extends CalamityShaders.CalamityShaderInstance {
    static DraedonBarrierRenderer shieldEffect;
    private final Uniform noiseScale;
    private final Uniform shieldEdgeColor;

    protected DraedonBarrierRenderer(ResourceProvider provider) throws IOException {
        super(provider, CalamityCurios.ModResource("barrier"), DefaultVertexFormat.POSITION_COLOR_TEX);
        shieldEffect = this;
        shieldEdgeColor = getUniform("ShieldEdgeColor");
        noiseScale = getUniform("NoiseScale");
    }

    public static void setUniform(float noiseScale, int shieldEdgeColor) {
        shieldEffect.noiseScale.set(noiseScale);
        shieldEffect.shieldEdgeColor.set(FastColor.ARGB32.red(shieldEdgeColor) / 255f,
            FastColor.ARGB32.green(shieldEdgeColor) / 255f, FastColor.ARGB32.blue(shieldEdgeColor) / 255f);
    }
}
