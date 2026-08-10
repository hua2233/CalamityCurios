package hua223.calamity.render.Item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.entity.projectiles.ZenithProjectile;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class TransformBakeModel implements BakedModel {
    protected final BakedModel originalModel;

    public TransformBakeModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    public static void register(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();

        //SetUp
        ZenithProjectile.Renderer.model = models.get(getInventory(CalamityItems.ZENITH));

        models.computeIfPresent(getInventory(CalamityItems.YHARIMS_CRYSTAL), (key , existingModel) -> {
            if (existingModel instanceof PrismModel) return existingModel;
            ItemOverrides.BakedOverride[] bakedOverrides = existingModel.getOverrides().overrides;
            ItemOverrides.BakedOverride override = bakedOverrides[0];

            return new PrismModel(existingModel, override.model);
        });

        models.computeIfPresent(getInventory(CalamityItems.STORM_MAIDENS_RETRIBUTION), (key , existingModel) ->
            existingModel instanceof StormMaidensModel ? existingModel : new StormMaidensModel(existingModel));

        models.computeIfPresent(getInventory(CalamityItems.DESTINY_BOOK), (key, bookExistingModel) ->
            new TransformBakeModel(bookExistingModel) {
            @Override
            public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
                return transformType == ItemDisplayContext.GUI ? this : applyDefaultTransform(transformType, poseStack, applyLeftHandTransform);
            }
        });
    }

    protected static ModelResourceLocation getInventory(CalamityItems item) {
        return new ModelResourceLocation(item.getId(), "inventory");
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
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
    @SuppressWarnings("deprecation")
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
