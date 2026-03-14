package hua223.calamity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@OnlyIn(Dist.CLIENT)
public class CalamityOutlineRenderer {
    private static List<OutlineInfo> highLightOutlines;
    private static int detectRadius;

    public static boolean notRender() {
        return highLightOutlines.isEmpty();
    }

    public static void renderOutlineList(Minecraft minecraft, PoseStack pose, PostChain chain, float partialTick) {
        Vec3 view = minecraft.gameRenderer.getMainCamera().getPosition();
        pose.translate(-view.x, -view.y, -view.z);

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);

        for (OutlineInfo outline : highLightOutlines) {
            pose.pushPose();
            pose.translate(outline.x, outline.y, outline.z);
            OutlineSource.setRenderColor(outline);
            if (outline.isBlock())
                dispatcher.renderSingleBlock(outline.state, pose, OutlineSource.source, 15728880,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            else
                entityRenderDispatcher.getRenderer(outline.entity).render(outline.entity,
                    outline.entity.getYRot(), partialTick, pose, OutlineSource.source, 15728880);

            pose.popPose();
        }

        OutlineSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        chain.process(partialTick);
    }

    public static void fromConfigLoad(int radius) {
        detectRadius = radius;
    }

    public static void init(Minecraft minecraft) {
        highLightOutlines = new CopyOnWriteArrayList<>();
        updateOutlineTarget(minecraft);
        new OutlineSource(minecraft);
    }

    public static void close() {
        highLightOutlines = null;
        OutlineSource.source = null;
    }

    @SuppressWarnings("ConstantConditions")
    public static void updateOutlineTarget(Minecraft minecraft) {
        AABB scope = minecraft.player.getBoundingBox().inflate(detectRadius);
        ClientLevel level = minecraft.level;
        highLightOutlines.clear();
        BlockPos.betweenClosedStream(scope)
            .forEach(pos -> {
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && level.hasChunk(pos.getX(), pos.getY())) {
                    if (state.is(Tags.Blocks.ORES)) highLightOutlines.add(new OutlineInfo(pos, state, false));
                    else if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity loot
                        && loot.getPersistentData().contains("LootTable"))
                        highLightOutlines.add(new OutlineInfo(pos, state, true));
                }
            });

        List<Entity> entities = level.getEntities(minecraft.player, scope);
        if (!entities.isEmpty())
            for (Entity entity : entities)
                if (entity instanceof Enemy || (entity instanceof Projectile &&
                    entity.getEntityData().get(CalamityHelp.CALAMITY_PROJECTILE_TAG)))
                    highLightOutlines.add(new OutlineInfo(entity));
    }
}

@OnlyIn(Dist.CLIENT)
class OutlineInfo {
    public final double x;
    public final double y;
    public final double z;
    public final BlockState state;
    public final Entity entity;
    public final int r;
    public final int g;
    public final int b;
    public final int a;

    public OutlineInfo(Entity entity) {
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
        state = null;
        this.entity = entity;
        r = 255;
        g = 0;
        b = 0;
        a = 255;
    }

    public OutlineInfo(BlockPos pos, BlockState state, boolean isGold) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.state = state;
        r = 255;
        a = 255;
        if (isGold) {
            g = 170;
            b = 0;
        } else {
            g = 255;
            b = 255;
        }

        entity = null;
    }

    boolean isBlock() {
        return state != null;
    }
}

@OnlyIn(Dist.CLIENT)
class OutlineSource implements MultiBufferSource {
    static OutlineSource source;
    final MultiBufferSource.BufferSource bufferSource;
    final IllusionBufferSource.IllusionVertexConsumer generator;

    OutlineSource(Minecraft minecraft) {
        this.bufferSource = minecraft.renderBuffers().bufferSource();
        this.generator = new IllusionBufferSource.IllusionVertexConsumer();
        source = this;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return generator.setDelegate(bufferSource.getBuffer(renderType));
    }

    static void setRenderColor(OutlineInfo info) {
        source.generator.defaultColor(info.r, info.g, info.b, info.a);
    }

    static void endBatch() {
        source.bufferSource.endBatch();
    }
}
