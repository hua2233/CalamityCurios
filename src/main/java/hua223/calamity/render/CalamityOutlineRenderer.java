package hua223.calamity.render;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
public class CalamityOutlineRenderer {
    private static Minecraft minecraft;
    private static PostChain effect;
    private static RenderTarget renderTarget;
    private static List<BooleanSupplier> runState;
    private static Queue<OutlineInfo> renderQueue;
    private static Queue<OutlineInfo> poolQueue;
    private static PoseStack pose;

    public static void start(BooleanSupplier runFunction) {
        if (minecraft == null) {
            try {
                minecraft = Minecraft.getInstance();
                effect = new PostChain(minecraft.textureManager, minecraft.getResourceManager(),
                    minecraft.getMainRenderTarget(), CalamityCurios.ModResource("shaders/post/outline.json"));
                effect.resize(minecraft.getWindow().getScreenWidth(), minecraft.getWindow().getScreenHeight());
                renderTarget = effect.getTempTarget("final");
                runState = new ArrayList<>();
                IllusionBufferSource.create();
                poolQueue = new ArrayDeque<>(30);
                pose = new PoseStack();
                renderQueue = Queues.newConcurrentLinkedQueue();
                runTick();
                runState.add(runFunction);
            } catch (IOException e) {
                stop();
                CalamityCurios.LOGGER.error("Cannot find outline shader file!!!");
            }
        }
    }

    private static void stop() {
        RenderSystem.recordRenderCall(() -> {
            minecraft = null;
            if (effect != null) effect.close();
            effect = null;
            renderTarget = null;
            runState = null;
            renderQueue = null;
            poolQueue = null;
            pose = null;
            IllusionBufferSource.destroy();
        });
    }

    private static void runTick() {
        DelayRunnable.conditionsLoop(() -> {
            poolQueue.addAll(renderQueue);
            renderQueue.clear();

            if (checkRunState()) {
                stop();
                return true;
            }

            return minecraft.player == null;
        }, 1);
    }

    private static boolean checkRunState() {
        runState.removeIf(BooleanSupplier::getAsBoolean);
        return runState.isEmpty();
    }

    public static void renderPerspective(RenderLevelStageEvent event) {
        if (renderQueue == null) return;

        RenderLevelStageEvent.Stage stage = event.getStage();
        //Render At The End To Prevent Contamination Of Other RenderTarget
        if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            if (!renderQueue.isEmpty()) {
                float partialTick = event.getPartialTick();
                RenderSystem.disableBlend();
                RenderSystem.disablePolygonOffset();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                fboSetUp();
                Vec3 view = event.getCamera().getPosition();
                pose.translate(-view.x, -view.y, -view.z);
                MultiBufferSource buffer = IllusionBufferSource.getSource(minecraft.renderBuffers().bufferSource());

                for (OutlineInfo outline : renderQueue) {
                    IllusionBufferSource.setColor(outline.r,  outline.g, outline.b, outline.a);
                    if (outline.state != null) {
                        pose.translate(outline.x, outline.y, outline.z);
                        minecraft.getBlockRenderer().renderSingleBlock(outline.state, pose, buffer, 15728880,
                            OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
                        pose.translate(-outline.x, -outline.y, -outline.z);
                    } else {
                        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
                        dispatcher.setRenderShadow(false);
                        Entity entity = outline.entity;
                        minecraft.getEntityRenderDispatcher().render(entity,
                            Mth.lerp(partialTick, entity.xOld, entity.getX()),
                            Mth.lerp(partialTick, entity.yOld, entity.getY()),
                            Mth.lerp(partialTick, entity.zOld, entity.getZ()),
                            Mth.lerp(partialTick, entity.yRotO, entity.getYRot()),
                            partialTick, pose, buffer, 15728880);
                        dispatcher.setRenderShadow(true);
                    }
                }

                minecraft.renderBuffers().bufferSource().endBatch();
                effect.process(partialTick);
                pose.translate(view.x, view.y, view.z);

                minecraft.getMainRenderTarget().bindWrite(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                renderTarget.blitToScreen(renderTarget.width, renderTarget.height, false);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                //Restore the original projection matrix
//                RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);
            }
        } else if(stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            PoseStack stack = event.getPoseStack();
            pose.last().pose().set(stack.last().pose());
            pose.last().normal().set(stack.last().normal());
        }
    }

    private static void fboSetUp() {
        Window window = minecraft.getWindow();
        int width = window.getScreenWidth();
        int height = window.getScreenHeight();
        if ((width != renderTarget.width || height != renderTarget.height) && width * height > 0)
            effect.resize(width, height);
        renderTarget.clear(Minecraft.ON_OSX);
        renderTarget.bindWrite(false);
    }

    public static void addRenderTarget(BlockPos pos, BlockState state, int color) {
       renderQueue.add((poolQueue.isEmpty() ? new OutlineInfo() : poolQueue.poll()).setBlockInfo(pos, state, color));
    }

    public static void addRenderTarget(Entity target, int color) {
        renderQueue.add((poolQueue.isEmpty() ? new OutlineInfo() : poolQueue.poll()).setEntity(target, color));
    }

    @OnlyIn(Dist.CLIENT)
    private static class OutlineInfo {
        private double x;
        private double y;
        private double z;
        private BlockState state;
        private Entity entity;
        private int r;
        private int g;
        private int b;
        private int a;

        private OutlineInfo setBlockInfo(BlockPos pos, BlockState state, int color) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.state = state;
            a = FastColor.ARGB32.alpha(color);
            r = FastColor.ARGB32.red(color);
            g = FastColor.ARGB32.green(color);
            b = FastColor.ARGB32.blue(color);

            entity = null;
            return this;
        }

        private OutlineInfo setEntity(Entity entity, int color) {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
            state = null;
            this.entity = entity;
            a = FastColor.ARGB32.alpha(color);
            r = FastColor.ARGB32.red(color);
            g = FastColor.ARGB32.green(color);
            b = FastColor.ARGB32.blue(color);
            return this;
        }

        private OutlineInfo() {}
    }
}