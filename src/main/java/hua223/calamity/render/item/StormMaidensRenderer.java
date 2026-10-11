package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.render.IllusionBufferSource;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
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
            StormMaidensModel model = (StormMaidensModel) renderer.getItemModelShaper().getItemModel(stack);
            float alpha = 1f - stack.getOrCreateTag().getFloat("alpha");

            //Render Glow Layer
            Vector2f drawOffset = new Vector2f();
            float offSetFactor = Mth.lerp(alpha, .015f, .022f);
            buffer = IllusionBufferSource.getSource((int) (Mth.lerp(alpha, 205, 245) * .4f),
                (int) (Mth.lerp(alpha, 92, 222) * .4f), (int) (Mth.lerp(alpha, 92, 179) * .4f), 20);
            for (int i = 0; i < 10; i++) {
                Vector2f.toRotationVector2(Mth.TWO_PI * i / 10f, drawOffset);
                drawOffset.mul(offSetFactor);
                poseStack.translate(drawOffset.x, drawOffset.y, 0);
                RenderUtil.renderItemModelList(renderer, model, stack, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay);
                poseStack.translate(-drawOffset.x, -drawOffset.y, 0);
            }
        } else {
            //Render Default Model
            RenderUtil.renderItemModelList(renderer, defaultModel == null ? renderer.getModel(stack,
                null, null, 0) : defaultModel, stack, poseStack, buffer, packedLight, packedOverlay);
            //From Forge Pop
        }
    }
}
