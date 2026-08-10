package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StormMaidensModel extends TransformBakeModel {
    private static BakedModel mainModel;

    private final ItemOverrides overrides = new StormMaidensOverride();

    protected StormMaidensModel(BakedModel originalModel) {
        super(originalModel.getOverrides().overrides[0].model);
        mainModel = originalModel;
    }

    @Override
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        if (transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            ItemTransform transform = originalModel.getTransforms().getTransform(transformType);
            float rx = transform.rotation.x;
            float ty = transform.translation.y;
            transform.rotation.x = -135;
            transform.translation.y = -0.7f;

            originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
            transform.rotation.x = rx;
            transform.translation.y = ty;

            return this;
        }
        return applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    public static BakedModel getMainModel() {
        return mainModel;
    }

    @Override
    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack stack, boolean fabulous) {
        return List.of(RenderUtil.Shaders.getStormMaidensGlow());
    }

    @OnlyIn(Dist.CLIENT)
    private class StormMaidensOverride extends ItemOverrides {
        @Override
        public @Nullable BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return entity != null && entity.isUsingItem() ? StormMaidensModel.this : mainModel;
        }
    }
}