package hua223.calamity.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PlayerLayerRender extends RenderLayer<Player, HumanoidModel<Player>> {
    private final HumanoidModel<Player> freeze;
    private final HumanoidModel<Player> crescent;

    @SuppressWarnings("unchecked")
    public PlayerLayerRender(RenderLayerParent renderer) {
        super(renderer);
        EntityModelSet set = Minecraft.getInstance().getEntityModels();
        freeze = new HumanoidModel<>(set.bakeLayer(getLayerLocation("frozen_layer")));
        crescent = new HumanoidModel<>(set.bakeLayer(getLayerLocation("crescent_stance")));
    }

    public static ModelLayerLocation getLayerLocation(String location) {
        return new ModelLayerLocation(CalamityCurios.ModResource(location), "main");
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int i, Player player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
        if (player.Calamity$Player.freeze) {
            getParentModel().copyPropertiesTo(freeze);
            VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(CalamityCurios.ResourceOf("minecraft", "textures/block/ice.png")));
            freeze.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
            freeze.renderToBuffer(poseStack, consumer, i, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        } else if (player.Calamity$Player.crescent) {
            float offset = (player.tickCount + partialTicks) * .01f;
            crescent.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
            getParentModel().copyPropertiesTo(crescent);
            crescent.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
            crescent.renderToBuffer(poseStack, buffer.getBuffer(RenderType.energySwirl(
                CalamityCurios.ResourceOf("minecraft", "textures/entity/creeper/creeper_armor.png"),
                offset * 3f, offset)), i, OverlayTexture.NO_OVERLAY, 0.85F, .15F, .15F, 1.0F);
        }
    }
}
