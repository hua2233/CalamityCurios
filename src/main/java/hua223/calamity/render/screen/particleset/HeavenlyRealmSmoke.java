package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4i;

import java.util.Queue;

@OnlyIn(Dist.CLIENT)
public class HeavenlyRealmSmoke extends ScreenParticleSet<HeavenlyRealmSmoke.BackgroundSmoke> {
    public short recycleTimer;
    protected float partialTick;

    protected final Vector4i lerpColor = new Vector4i();
    protected final Vector2f lerpPos = new Vector2f();
    protected final Quaternionf quaternion = new Quaternionf();

    public HeavenlyRealmSmoke() {
        super(0, 80, 80, 90);
    }

    public Queue<BackgroundSmoke> getParticles() {
        partialTick = Minecraft.getInstance().getPartialTick();
        return active;
    }

    @Override
    public void update() {
        for (byte i = 0; i < 6; i++) getNext();
        super.update();
    }

    @Override
    protected BackgroundSmoke getParticle() {
        return new BackgroundSmoke();
    }

    @OnlyIn(Dist.CLIENT)
    public class BackgroundSmoke extends ScreenParticle {
        protected float rotation;
        protected int lifeTime;
        protected float ro;
        protected final Vector2f posOld = new Vector2f();
        protected final Vector4i colorOld = new Vector4i();
        protected final Vector2f drawPosition = new Vector2f();
        protected final Vector2f velocity = new Vector2f();

        protected BackgroundSmoke() {
            super(null);
            reActive();
        }

        @Override
        protected void update() {
            posOld.set(drawPosition);
            drawPosition.add(velocity);
            velocity.mul(.983f);
            color.set(colorOld);
            color.div(0.997f);
            ro = rotation;
            rotation += velocity.x * 0.01f;
        }

        @Override
        public void baseDraw(VertexConsumer consumer) {
            RenderUtil.interpolateColor(colorOld, color, partialTick, lerpColor);
            if (lifeTime - time < 15) RenderUtil.multiplyColor(lerpColor, (lifeTime - time) / 15f, lerpColor);
            RenderUtil.multiplyColor(lerpColor, .44f, lerpColor);
            lerpPos.set(Mth.lerp(partialTick, posOld.x, drawPosition.x), Mth.lerp(partialTick, posOld.y, drawPosition.y));
            RenderUtil.radianQuaternions(quaternion, CalamityHelp.UNIT_Z, Mth.lerp(partialTick, ro, rotation));
            Matrix4f matrix = getTransientMatrix().identity().translate(lerpPos.x, lerpPos.y, 0).rotate(quaternion).scale(1.59f);

            for (Vector4i rotate : textureInfo)
                consumer.vertex(matrix, 128 * rotate.x, 128 * rotate.y, 0f)
                    .color(lerpColor.x, lerpColor.y, lerpColor.z, lerpColor.w).uv(rotate.z, rotate.w).endVertex();
        }

        @Override
        protected int getLifetime() {
            return lifeTime;
        }

        @Override
        protected void reActive() {
            RandomSource source = Minecraft.getInstance().font.random;
            drawPosition.set(Mth.randomBetween(source, -400, CalamityShaders.MAIN.width + 400), CalamityShaders.MAIN.height + 372f);
            posOld.set(drawPosition);
            velocity.vector2Circular(3f, 3f, source).add(0, -Mth.randomBetween(source, 5, 23));

            float gradient = 0.5f + source.nextFloat() * 0.35f;
            int c = (int) (255 * .9f);
            color.set(c, (int) (Mth.lerp(gradient, 127, 255) * 0.9f), (int) (Mth.lerp(gradient, 80, 255) * 0.9f), c);
            colorOld.set(color);
            rotation = source.nextFloat() * Mth.TWO_PI;
            time = 0;
            lifeTime = source.nextInt(40, 160);
        }
    }
}
