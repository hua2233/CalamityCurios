package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.*;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.render.shaders.StaticOverlayShader;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EmptinessSprayerRenderer extends BlockEntityWithoutLevelRenderer {
    private static TextureAtlasSprite sprite;
    private static TransformBakeModel model;

    public EmptinessSprayerRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
    }

    public static TransformBakeModel replaceModel(BakedModel originalModel) {
        model = new TransformBakeModel(originalModel) {
            @Override
            public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
                return List.of(CalamityShaders.getStaticOverlay(InventoryMenu.BLOCK_ATLAS, fabulous));
            }

            @Override
            public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
                applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
                return this;
            }
        };
        sprite = model.getParticleIcon();
        return model;
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        StaticOverlayShader.setRate(16.7f);
        StaticOverlayShader.setZoom(sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
        RenderUtil.renderItemModelList(Minecraft.getInstance().getItemRenderer(), model, stack, poseStack, buffer, packedLight, packedOverlay);
    }
}
