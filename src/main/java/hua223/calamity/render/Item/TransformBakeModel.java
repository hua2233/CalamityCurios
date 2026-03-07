package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class TransformBakeModel implements BakedModel {
    private final BakedModel originalModel;

    public TransformBakeModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    public static void register(ModelEvent.BakingCompleted event) throws Exception {
        final String itemModelPath = "inventory";

        ModelResourceLocation bookKey = new ModelResourceLocation(CalamityItems.DESTINY_BOOK.getId(), itemModelPath);
        BakedModel bookExistingModel = event.getModels().get(bookKey);
        if (bookExistingModel != null) event.getModels().put(bookKey,
            new TransformBakeModel(bookExistingModel) {
                @Override
                public @NotNull BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
                    return transformType == ItemTransforms.TransformType.GUI ?
                        this : applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
                }
            });

        ModelResourceLocation yharimsKey = new ModelResourceLocation(CalamityItems.YHARIMS_CRYSTAL.getId(), itemModelPath);
        BakedModel yharimsExistingModel = event.getModels().get(yharimsKey);
        if (yharimsExistingModel != null) {
            ItemOverrides.BakedOverride[] bakedOverrides = yharimsExistingModel.getOverrides().overrides;
            if (bakedOverrides.length == 0) return;

            ItemOverrides.BakedOverride override = bakedOverrides[0];
            BakedModel originalModel = override.model;
            if (originalModel == null || originalModel instanceof TransformBakeModel) return;

            bakedOverrides[0] = new ItemOverrides.BakedOverride(
                override.matchers, new TransformBakeModel(originalModel) {
                @Override
                public @NotNull BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
                    switch (transformType) {
                        case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                            return YharimsCrystalRenderer.updateModelTransform(poseStack, originalModel, transformType);
                        }

                        default -> {
                            return applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
                        }
                    }
                }
            });
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return originalModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    protected final BakedModel applyDefaultTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public ItemOverrides getOverrides() {
        return originalModel.getOverrides();
    }

    @Override
    public abstract BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform);
}
