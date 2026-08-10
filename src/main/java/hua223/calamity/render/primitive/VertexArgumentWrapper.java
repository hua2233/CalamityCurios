package hua223.calamity.render.primitive;

import hua223.calamity.util.Vector2f;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class VertexArgumentWrapper {
    public Vector2f position;// X, Y, Z
    public int r;
    public int g;
    public int b;
    public int a;
    public Vector2f uv;// UV
    public int w;

    public VertexArgumentWrapper(Vector2f position, Vector4i color, Vector2f uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = color.x();
        this.g = color.y();
        this.b = color.z();
        this.a = color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }

    public void reLoad(Vector2f position, Vector4i color, Vector2f uv, float widthCorrectionFactor) {
        this.position = position;
        this.r = color.x();
        this.g = color.y();
        this.b = color.z();
        this.a = color.w();
        this.uv = uv;
        this.w = (int) (widthCorrectionFactor * 3000);
    }
}
