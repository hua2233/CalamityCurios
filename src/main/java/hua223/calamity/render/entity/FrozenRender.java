package hua223.calamity.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrozenRender extends RenderLayer<Player, HumanoidModel<Player>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CalamityCurios.ModResource("frozen_layer"), "main");
    private static final ResourceLocation FROZEN_TEXTURE = CalamityCurios.ResourceOf("minecraft", "textures/block/ice.png");

    public final HumanoidModel<Player> model;

    public FrozenRender(RenderLayerParent renderer) {
        super(renderer);
        model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int i, Player player, float v, float v1, float v2, float v3, float v4, float v5) {
        if (player.calamity$IsFreeze) {
            getParentModel().copyPropertiesTo(model);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FROZEN_TEXTURE));
            model.setupAnim(player, v, v1, v3, v4, v5);
            model.renderToBuffer(poseStack, consumer, i, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
