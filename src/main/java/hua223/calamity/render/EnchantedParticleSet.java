package hua223.calamity.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class EnchantedParticleSet {
    static final float INTERPOLATION_SPEED = 0.2f;
    static final int EDGE_OFFSET = 18;
    private static final int PARTICLE_LIFETIME = 17;
    private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/gui/light.png");
    public static boolean canUpdate;
    public static byte count;
    public static boolean isInventory;
    private static Queue<GuiEnchantedParticle> active;
    private static Queue<GuiEnchantedParticle> pool;
    private static Random random;

    public static void update() {
        if (canUpdate) {
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

            canUpdate = false;
        }
    }

    public static void drawSet(int x, int y, float blitOffset) {
        if (isInventory) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            //RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            for (GuiEnchantedParticle particle : active) {
                double drawPositionX = x - particle.relativeOffset.x;
                double drawPositionY = y - particle.relativeOffset.y;
                int r = (int) particle.color.x();
                int g = (int) particle.color.y();
                int b = (int) particle.color.z();
                int a = (int) particle.color.w();
                float scale = particle.scale;
                float depth = particle.depth - blitOffset;
                buffer.vertex(drawPositionX - scale, drawPositionY + scale, depth)
                    .color(r, g, b, a)
                    .uv(0, 0)
                    .endVertex();

                buffer.vertex(drawPositionX + scale, drawPositionY + scale, depth)
                    .color(r, g, b, a)
                    .uv(0, 1)
                    .endVertex();

                buffer.vertex(drawPositionX + scale, drawPositionY - scale, depth)
                    .color(r, g, b, a)
                    .uv(1, 1)
                    .endVertex();

                buffer.vertex(drawPositionX - scale, drawPositionY - scale, depth)
                    .color(r, g, b, a)
                    .uv(1, 0)
                    .endVertex();
            }

            tesselator.end();
            RenderSystem.defaultBlendFunc();
            //RenderSystem.depthMask(true);
        }
    }

    public static void initializationParticlePool() {
        if (active == null) {
            active = new ArrayDeque<>(80);
            pool = new ArrayDeque<>(100);
            random = new Random();
        }
    }

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
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static class GuiEnchantedParticle {
        private final Vector2d relativeOffset = new Vector2d(0, 0);
        private final Vector4f color = RenderUtil.black();
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
            color.set(RenderUtil.DARK_VIOLET.x(), RenderUtil.DARK_VIOLET.y(), RenderUtil.DARK_VIOLET.z(), 200f);
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
                    color.setZ(color.z() - 15f);
                    if (color.z() < 50f) color.setZ(0f);
                }
            }

            RenderUtil.interpolateColor(RenderUtil.DARK_VIOLET, RenderUtil.WHITE, RenderUtil.clampLerp(
                0f, 0.67f, time / PARTICLE_LIFETIME, true), color);
        }
    }
}
