package hua223.calamity.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public abstract class ScreenEffectRenderer implements Comparable<ScreenEffectRenderer> {
    private static final ArrayList<ScreenEffectRenderer> EFFECTS = new ArrayList<>();
    private byte tickState;

    protected ScreenEffectRenderer() {
        EFFECTS.add(this);
        boolean runTick = enableTick();
        if (EFFECTS.size() != 1) {
            ScreenEffectRenderer last = EFFECTS.get(EFFECTS.size() - 1);
            EFFECTS.sort(ScreenEffectRenderer::compareTo);
            ScreenEffectRenderer effect = EFFECTS.get(EFFECTS.size() - 1);
            if (last != effect) {
                last.tickState = -1;
                runTick = effect == this && runTick;
            }
        }

        if (runTick && tickState == 0) {
            tickState = 1;
            Minecraft minecraft = Minecraft.getInstance();
            DelayRunnable.addUniqueLoopTask(() -> {
                if (tickState < 0 || minecraft.player == null) {
                    tickState = 0;
                    return true;
                }

                tick();
                return false;
            }, 1, getClass());
        }
    }

    public static void preScreenRender(float partialTick, Minecraft minecraft) {
        if (!EFFECTS.isEmpty()) {
           ScreenEffectRenderer renderer = EFFECTS.get(EFFECTS.size() - 1);
           if (renderer.render(partialTick, minecraft)) {
               EFFECTS.remove(EFFECTS.size() - 1);
           }
        }
    }

    protected boolean enableTick() {
        return false;
    }

    public void tick() {}

    public abstract boolean render(float partialTick, Minecraft minecraft);

    public abstract int getPriority();

    protected static void renderMask(Minecraft minecraft) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderOnlyTexture(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    protected static void renderOnlyTexture(float width, float height) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    @Override
    public int compareTo(@NotNull ScreenEffectRenderer renderer) {
        return Integer.compare(getPriority(), renderer.getPriority());
    }
}
