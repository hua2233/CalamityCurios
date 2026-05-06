package hua223.calamity.util.primitive;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class VertexArgumentWrapper {
    public Vec3 position;// X, Y, Z
    public int r;
    public int g;
    public int b;
    public int a;
    public Vec2 uv;// UV
    public int w;

    public VertexArgumentWrapper(Vec3 position, Vector4i color, Vec2 uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = color.x();
        this.g = color.y();
        this.b = color.z();
        this.a = color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }

    public void reLoad(Vec3 position, Vector4i color, Vec2 uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = color.x();
        this.g = color.y();
        this.b = color.z();
        this.a = color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }
}
