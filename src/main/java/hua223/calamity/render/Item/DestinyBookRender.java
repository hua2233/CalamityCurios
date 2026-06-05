package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.integration.curios.item.DestinyBook;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DestinyBookRender extends BlockEntityWithoutLevelRenderer {
    public DestinyBookRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext,
                             PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = renderer.getModel(stack, null, null, 0);

        poseStack.pushPose();
        model.getTransforms().getTransform(displayContext).apply(false, poseStack);

        RenderUtil.renderItemModelList(renderer, model, stack, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        if (displayContext == ItemDisplayContext.GUI) {
            ItemStack overlay = DestinyBook.getOverlayStack(stack);

            if (overlay != null && !overlay.isEmpty()) {
                BakedModel overlayModel = renderer.getModel(overlay, null, null, 0);
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 1);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                RenderUtil.renderItemModelList(renderer, overlayModel, overlay, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }
}
