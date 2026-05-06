package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.entity.projectiles.ZenithProjectile;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
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

    public static void register(ModelEvent.ModifyBakingResult event) {
        final String itemModelPath = "inventory";
        //SetUp
        ZenithProjectile.ZenithProjectileRenderer.model =
            event.getModels().get(new ModelResourceLocation(CalamityItems.ZENITH.getId(), itemModelPath));

        ModelResourceLocation bookKey = new ModelResourceLocation(CalamityItems.DESTINY_BOOK.getId(), itemModelPath);
        event.getModels().computeIfPresent(bookKey, (key, bookExistingModel) ->
            new TransformBakeModel(bookExistingModel) {
            @Override
            public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
                return transformType == ItemDisplayContext.GUI ?
                    this : applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
            }
        });

        ModelResourceLocation yharimsKey = new ModelResourceLocation(CalamityItems.YHARIMS_CRYSTAL.getId(), itemModelPath);
        event.getModels().computeIfPresent(yharimsKey, (key , yharimsExistingModel) -> {
            ItemOverrides.BakedOverride[] bakedOverrides = yharimsExistingModel.getOverrides().overrides;
            if (bakedOverrides.length != 0)  {
                ItemOverrides.BakedOverride override = bakedOverrides[0];
                BakedModel originalModel = override.model;
                if (originalModel != null && !(originalModel instanceof TransformBakeModel)) {
                    bakedOverrides[0] = new ItemOverrides.BakedOverride(
                        override.matchers, new TransformBakeModel(originalModel) {
                        @Override
                        public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull
                        PoseStack poseStack, boolean applyLeftHandTransform) {
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
            return yharimsExistingModel;
        });
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
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
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    protected final BakedModel applyDefaultTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return originalModel.getOverrides();
    }

    @Override
    public abstract @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform);
}
