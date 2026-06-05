package hua223.calamity.render;

import com.mojang.blaze3d.vertex.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class EnchantedParticleSet {
    private static final float INTERPOLATION_SPEED = 0.2f;
    private static final int EDGE_OFFSET = 18;
    private static final int PARTICLE_LIFETIME = 17;
    public static byte count;
    public static boolean isInventory;
    private static Queue<GuiEnchantedParticle> active;
    private static Queue<GuiEnchantedParticle> pool;
    private static RenderType type;
    private static Random random;

    public static void update() {
        //Spawn new particles if time remains
        for (byte i = 0; i < 4; i++) getNext();

        //Update and increment the time of all particles
        Iterator<GuiEnchantedParticle> iterator = active.iterator();
        while (iterator.hasNext()) {
            GuiEnchantedParticle particle = iterator.next();

            particle.time++;
            if (particle.time < PARTICLE_LIFETIME) {
                particle.update();
            } else {
                pool.offer(particle);
                iterator.remove();
            }
        }
    }

    public static void drawSet(int x, int y, GuiGraphics graphics) {
        if (isInventory) {
            VertexConsumer consumer = graphics.bufferSource().getBuffer(type);
            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate(x + 8, y + 8, 0);
            Matrix4f sourceMatrix = stack.last().pose();
            Matrix4f transientMatrix = RenderUtil.TRANSIENT_MATRIX;
            for (GuiEnchantedParticle particle : active) {
                //Using preset matrices to avoid creating a large number of duplicate matrix objects
                transientMatrix.set(sourceMatrix);
                transientMatrix.translate((float) -particle.relativeOffset.x, (float) -particle.relativeOffset.y, -particle.depth);
                int r = particle.color.x;
                int g = particle.color.y;
                int b = particle.color.z;
                int a = particle.color.w;
                float scale = particle.scale;
                consumer.vertex(transientMatrix, -scale, scale, 0)
                    .color(r, g, b, a)
                    .uv(0, 0)
                    .endVertex();

                consumer.vertex(transientMatrix, scale, scale, 0)
                    .color(r, g, b, a)
                    .uv(0, 1)
                    .endVertex();

                consumer.vertex(transientMatrix, scale, -scale, 0)
                    .color(r, g, b, a)
                    .uv(1, 1)
                    .endVertex();

                consumer.vertex(transientMatrix, -scale, -scale, 0)
                    .color(r, g, b, a)
                    .uv(1, 0)
                    .endVertex();
            }

            stack.popPose();
        }
    }

    public static void initializationParticlePool() {
        if (active == null) {
            active = new ArrayDeque<>(80);
            pool = new ArrayDeque<>(100);
            random = new Random();
            type = RenderUtil.Shaders.getEnchanmentRenderType(
                CalamityCurios.ModResource("textures/calamity_gui/light.png"));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void getNext() {
        if (count < 80) {
            active.offer(new GuiEnchantedParticle().active());
            ++count;
        } else active.offer(pool.poll().active());
    }

    public static void close() {
        //Prohibit setting to null while currently rendering loop
        //If you open the inventory interface again within two seconds, it will not initialize again
        DelayRunnable.addOrReset(40, EnchantedParticleSet.class, () -> {
            if (!(Minecraft.getInstance().screen instanceof InventoryScreen)) {
                pool = null;
                active = null;
                random = null;
                count = 0;
                type = null;
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static class GuiEnchantedParticle {
        private final Vector2d relativeOffset = new Vector2d(0, 0);
        private final Vector4i color = RenderUtil.black();
        private float depth;
        private float scale = 1.5f;
        private float time;

        private GuiEnchantedParticle() {
        }

        private GuiEnchantedParticle active() {
            relativeOffset.nextVector2Circular(1f,
                1f, random).mul(EDGE_OFFSET);
            scale = 1.5f;
            time = 0;
            depth = 0;
            color.set(RenderUtil.DARK_VIOLET.x(), RenderUtil.DARK_VIOLET.y(), RenderUtil.DARK_VIOLET.z(), 200);
            return this;
        }

        public void update() {
            float distanceToCenter = relativeOffset.length();
            scale = RenderUtil.smoothStep(1f, 1.85f, RenderUtil.clampLerp(EDGE_OFFSET, 6f, distanceToCenter, true));
            scale *= RenderUtil.clampLerp(PARTICLE_LIFETIME, PARTICLE_LIFETIME - 4f, time, true);

            if (distanceToCenter > 3.5f) {
                depth = 2;
                relativeOffset.lerp(Vector2d.ZERO, INTERPOLATION_SPEED);
            } else {
                depth = 0;
                if (color.z() > 50f) {
                    color.z = (color.z() - 15);
                    if (color.z() < 50f) color.z = 0;
                }
            }

            RenderUtil.interpolateColor(RenderUtil.DARK_VIOLET, RenderUtil.WHITE, RenderUtil.clampLerp(
                0f, 0.67f, time / PARTICLE_LIFETIME, true), color);
        }
    }
}
