package hua223.calamity.util.primitive;

import com.mojang.math.Vector4f;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexArgumentWrapper {
    public Vec3 position;// X, Y, Z
    public int r;
    public int g;
    public int b;
    public int a;
    public Vec2 uv;// UV
    public int w;

    public VertexArgumentWrapper(Vec3 position, Vector4f color, Vec2 uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = (int) color.x();
        this.g = (int) color.y();
        this.b = (int) color.z();
        this.a = (int) color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }

    public void reLoad(Vec3 position, Vector4f color, Vec2 uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = (int) color.x();
        this.g = (int) color.y();
        this.b = (int) color.z();
        this.a = (int) color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }
}
