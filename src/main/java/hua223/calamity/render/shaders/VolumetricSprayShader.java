package hua223.calamity.render.shaders;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Vector3f;

import java.io.IOException;

public class VolumetricSprayShader extends CalamityShaders.CalamityShaderInstance {
    private static VolumetricSprayShader INST;
    private final Uniform HALF_SIZE;
    private final Uniform NOZZLE_POS;
    private final Uniform JET_DIR;
    private final Uniform JET_LENGTH;
    protected VolumetricSprayShader(ResourceProvider provider) throws IOException {
        super(provider, CalamityCurios.ModResource("volumetric_spray"), DefaultVertexFormat.POSITION);
        INST = this;
        HALF_SIZE = getUniform("HalfSize");
        NOZZLE_POS = getUniform("NozzlePos");
        JET_DIR = getUniform("JetDir");
        JET_LENGTH = getUniform("JetLength");
    }
    public static void setHalfSize(float h) { INST.HALF_SIZE.set(h); }


    public static void setJetLength(float len) {
        INST.JET_LENGTH.set(len);
    }

    public static void setJetDir(float x, float y, float z) {
        INST.JET_DIR.set(x, y, z);
    }

    public static void setNozzlePos(Vector3f pos) {
        INST.NOZZLE_POS.set(pos.x, pos.y, pos.z);
    }
}
