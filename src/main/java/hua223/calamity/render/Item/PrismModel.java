package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrismModel extends TransformBakeModel {
    private IPrismRender render;
    private final BakedModel overrideModel;
    private final PrismOverrideModel model;

    protected PrismModel(BakedModel originalModel, BakedModel overrideModel) {
        super(originalModel);
        model = new PrismOverrideModel();
        this.overrideModel = overrideModel;
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        switch (transformType) {
            case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND : {
                if (render != null) {
                    render.updateModelTransform(poseStack, overrideModel, transformType);
                    render = null;
                    return overrideModel;
                }
            }

            default : return applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
        }
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    private class PrismOverrideModel extends ItemOverrides {
        @Override
        public @Nullable BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            if (entity != null && entity.isUsingItem() && entity.calamity$IsPlayer) {
                PrismModel.this.render = entity.calamity$Player.Calamity$Player.getRenderer();
                return PrismModel.this;
            }

            return originalModel;
        }
    }
}
