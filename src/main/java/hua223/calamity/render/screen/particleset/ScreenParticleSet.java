package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("rawtypes")
public abstract class ScreenParticleSet <T extends ScreenParticleSet.ScreenParticle> {
    protected final int PARTICLE_LIFETIME;
    protected final int MAX_NUMBER;
    private short count;
    private final Vector4i[] textureInfo = new Vector4i[] {
        new Vector4i(-1, 1, 0, 0),
        new Vector4i(1, 1, 0, 1),
        new Vector4i(1, -1, 1, 1),
        new Vector4i(-1, -1, 1, 0)
    };

    protected final Queue<T> active;
    protected final Queue<T> pool;

    public ScreenParticleSet(int lifetime, int maxCount, int activeCapacity, int poolCapacity) {
        PARTICLE_LIFETIME = lifetime;
        this.MAX_NUMBER = maxCount;
        this.active = new ArrayDeque<>(activeCapacity);
        this.pool = new ArrayDeque<>(poolCapacity);
    }

    public void update() {
        Iterator<T> iterator = active.iterator();
        while (iterator.hasNext()) {
            T screenParticle = iterator.next();
            screenParticle.time++;
            if (screenParticle.time < PARTICLE_LIFETIME) {
                screenParticle.lifetimeRatio = screenParticle.time / PARTICLE_LIFETIME;
                screenParticle.update();
            } else {
                iterator.remove();
                pool.add(screenParticle);
            }
        }
    }

    protected abstract T getParticle();

    @SuppressWarnings("ConstantConditions")
    protected void getNext() {
        if (!pool.isEmpty()) {
            T p = pool.poll();
            p.reActive();
            active.offer(p);
        } if (count < MAX_NUMBER) {
            active.offer(getParticle());
            ++count;
        }
    }

    protected Matrix4f getTransientMatrix() {
        return RenderUtil.TRANSIENT_MATRIX;
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract class ScreenParticle {
        protected final Vector2f size;

        protected final Vector4i color = RenderUtil.black();
        protected float time;
        protected float lifetimeRatio;

        protected ScreenParticle(Vector2f size) {
            if (textureInfo.length != 4)
                throw new IllegalStateException("Incomplete texture uv info sequence!");
            this.size = size;
        }

        protected abstract void update();

        protected abstract void reActive();

        protected Vector2f getSize() {
            return size;
        }

        public void baseDraw(VertexConsumer consumer) {
            Vector2f scale = getSize();
            for (Vector4i rotate : textureInfo) {
                consumer.vertex(RenderUtil.TRANSIENT_MATRIX, scale.x * rotate.x, scale.y * rotate.y, -90f)
                    .color(color.x, color.y, color.z, color.w).uv(rotate.z, rotate.w).endVertex();
            }
        }

        //The palette that the energy particle streaks can cycle through.
        protected void fromPalette(Vector4i[] palette, float interpolant) {
            // Apply interpolant safety checks.
            if (Float.isNaN(interpolant) || Float.isInfinite(interpolant)) interpolant = 0f;
            interpolant = Mth.clamp(interpolant, 0f, 0.999f);

            int gradientStartingIndex = (int)(interpolant * palette.length);
            float currentColorInterpolant = interpolant * palette.length % 1f;
            Vector4i gradientSubdivisionA = palette[gradientStartingIndex];
            Vector4i gradientSubdivisionB = palette[Mth.clamp(gradientStartingIndex + 1, 0, palette.length - 1)];
            RenderUtil.interpolateColor(gradientSubdivisionA, gradientSubdivisionB, currentColorInterpolant, color);
        }
    }
}
