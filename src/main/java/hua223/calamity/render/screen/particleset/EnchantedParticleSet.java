package hua223.calamity.render.screen.particleset;

import com.mojang.blaze3d.vertex.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class EnchantedParticleSet extends ScreenParticleSet<EnchantedParticleSet.GuiEnchantedParticle> {
    private static final float INTERPOLATION_SPEED = 0.2f;
    private static final int EDGE_OFFSET = 18;
    private static EnchantedParticleSet INSTANCE;
    public static boolean hasBrimstoneLocus;

    public boolean isInventory;
    private final Vector4i white;
    private final Vector4i darkViolet;
    private final RenderType type;
    private final RandomSource random;

    private EnchantedParticleSet(RandomSource source) {
        super(17, 80,  80, 100);
        white = RenderUtil.fromColorGet(Color.WHITE);
        darkViolet = new Vector4i(148, 0, 211, 255);
        type = RenderUtil.Shaders.getEnchanmentRenderType(
            CalamityCurios.ModResource("textures/calamity_gui/light.png"));
        random = source;
        INSTANCE = this;
    }

    @Override
    public void update() {
        if (hasBrimstoneLocus) {
            //Spawn new particles if time remains
            for (byte i = 0; i < 4; i++) getNext();
            //Update and increment the time of all particles
            super.update();
            hasBrimstoneLocus = false;
        }
    }

    @Override
    protected GuiEnchantedParticle getParticle() {
        return new GuiEnchantedParticle();
    }

    public static boolean drawSet(int x, int y, GuiGraphics graphics) {
        if (hasBrimstoneLocus && INSTANCE != null && INSTANCE.isInventory) {
            VertexConsumer consumer = graphics.bufferSource().getBuffer(INSTANCE.type);
            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate(x + 8, y + 8, 0);
            Matrix4f sourceMatrix = stack.last().pose();
            //Using preset matrices to avoid creating a large number of duplicate matrix objects
            Matrix4f transientMatrix = INSTANCE.getTransientMatrix();
            for (GuiEnchantedParticle particle : INSTANCE.active) {
                transientMatrix.set(sourceMatrix);
                transientMatrix.translate(-particle.relativeOffset.x, -particle.relativeOffset.y, -particle.depth);
                particle.baseDraw(consumer);
            }

            stack.popPose();
            return true;
        }

        return false;
    }

    public static EnchantedParticleSet getInstance() {
        return INSTANCE;
    }

    public static void create(RandomSource source) {
        if (INSTANCE != null) DelayRunnable.removeTask(EnchantedParticleSet.class);
        else new EnchantedParticleSet(source);
    }

    public static void close() {
        //Prohibit setting to null while currently rendering loop
        //If you open the inventory interface again within two seconds, it will not initialize again
        DelayRunnable.addOrReset(40, EnchantedParticleSet.class, () -> {
            if (!(Minecraft.getInstance().screen instanceof InventoryScreen)) INSTANCE = null;
        });
    }

    @OnlyIn(Dist.CLIENT)
    protected class GuiEnchantedParticle extends ScreenParticle {
        private final Vector2f relativeOffset = new Vector2f(0, 0);
        private float depth;

        private GuiEnchantedParticle() {
            super(new Vector2f(1.5f, 1.5f));
            reActive();
        }

        @Override
        protected void reActive() {
            relativeOffset.vector2Circular(1f, 1f, random).mul(EDGE_OFFSET);
            color.set(darkViolet.x(), darkViolet.y(), darkViolet.z(), 200);
            time = 0;
            depth = 0;
        }

        @Override
        public void update() {
            float distanceToCenter = relativeOffset.length();
            float s = RenderUtil.smoothStep(1f, 1.85f, RenderUtil.clampLerp(EDGE_OFFSET, 6f, distanceToCenter));
            size.set(s, s);
            size.mul(RenderUtil.clampLerp(PARTICLE_LIFETIME, PARTICLE_LIFETIME - 4f, time));

            if (distanceToCenter > 3.5f) {
                depth = 2;
                relativeOffset.lerp(Vector2f.ZERO, INTERPOLATION_SPEED);
            } else {
                depth = 0;
                if (color.z() > 50f) {
                    color.z = (color.z() - 15);
                    if (color.z() < 50f) color.z = 0;
                }
            }

            RenderUtil.interpolateColor(darkViolet, white, RenderUtil.clampLerp(
                0f, 0.67f, lifetimeRatio), color);
        }
    }
}
