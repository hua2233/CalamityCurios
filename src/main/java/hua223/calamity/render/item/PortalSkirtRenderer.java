package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.*;
import hua223.calamity.render.shaders.CalamityShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PortalSkirtRenderer extends BlockEntityWithoutLevelRenderer {
    public PortalSkirtRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderPortalSkirt(poseStack, buffer, 1.25f, false);
    }

    public static void renderPortalSkirt(PoseStack poseStack, MultiBufferSource buffer, float radius, boolean zDraw) {
        float[] vertex = zDraw ? new float[] {radius, 0, radius} : new float[] {radius, radius, 0};
        VertexConsumer consumer = buffer.getBuffer(CalamityShaders.getPortalSkirt());
        Matrix4f matrix4f = poseStack.last().pose();
        consumer.vertex(matrix4f, vertex[0], vertex[1], vertex[2]).uv(0, 1).endVertex();
        consumer.vertex(matrix4f, vertex[0], -vertex[1], -vertex[2]).uv(0, 0).endVertex();
        consumer.vertex(matrix4f, -vertex[0], -vertex[1], -vertex[2]).uv(1, 0).endVertex();
        consumer.vertex(matrix4f, -vertex[0], vertex[1], vertex[2]).uv(1, 1).endVertex();
    }
}
