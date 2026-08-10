package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.render.IllusionBufferSource;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class StormMaidensRenderer extends BlockEntityWithoutLevelRenderer {
    public StormMaidensRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel defaultModel = null;
        if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            float rawAlpha = stack.getTag().getFloat("RawAlpha");

            StormMaidensModel model = (StormMaidensModel) renderer.getItemModelShaper().getItemModel(stack);
            Vector4i backGlowColor = new Vector4i(205, 92, 92, 255);
            float alpha = 1f - rawAlpha;

            RenderUtil.multiplyColor(RenderUtil.interpolateColor(backGlowColor, new Vector4i(
                245, 222, 179, 255),  alpha * 0.56f, backGlowColor), 0.45f, backGlowColor);

            //Render Glow Layer
            Vector2f drawOffset = new Vector2f();
            IllusionBufferSource.setColor(backGlowColor.x, backGlowColor.y, backGlowColor.z, (int) Math.max(26, alpha * 255));
            buffer = IllusionBufferSource.getSource(buffer);
            for (int i = 0; i < 8; i++) {
                Vector2f.toRotationVector2(Mth.TWO_PI * i / 8f, drawOffset);
                drawOffset.mul(0.02f);
                poseStack.translate(drawOffset.x, drawOffset.y, 0);
                RenderUtil.renderItemModelList(renderer, model, stack, poseStack, buffer, packedLight, packedOverlay);
                poseStack.translate(-drawOffset.x, -drawOffset.y, 0);
            }

            if (rawAlpha == 0f) return;
            //Make Transparent
            IllusionBufferSource.setColor(-1f, -1f, -1f, rawAlpha);
            //Apply Custom Transform
            defaultModel = StormMaidensModel.getMainModel();
            //If Alpha is greater than 0, render the default model
        }

        //Render Default Model
        RenderUtil.renderItemModelList(renderer, defaultModel == null ? renderer.getModel(stack,
            null, null, 0) : defaultModel, stack, poseStack, buffer, packedLight, packedOverlay);
        //From Forge Pop
    }
}
