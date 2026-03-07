package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;
import java.util.UUID;

public class NihilityQuiver extends BaseCurio {
    public NihilityQuiver(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void ArrowSet(ProjectileSpawnListener listener) {
        if (listener.isArrow) {
            listener.hurtAmplifier += 0.75;
            listener.speedVectorAmplifier += 1;
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "quiver", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 2));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 1));
        }
        return tooltips;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render implements ICurioRenderer {
        private static final ResourceLocation TEXTURE = CalamityCurios.ModResource("textures/item/nihility_quiver.png");

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
            LivingEntity entity = slotContext.entity();
            ICurioRenderer.translateIfSneaking(matrixStack, entity);
            ICurioRenderer.rotateIfSneaking(matrixStack, entity);

            matrixStack.pushPose();
            matrixStack.translate(0, 0.25, 0.2);
            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));

            VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            Matrix4f matrix = matrixStack.last().pose();
            Matrix3f normal = matrixStack.last().normal();

            vertexBuilder.vertex(matrix, -0.5f, -0.5f, 0)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();

            vertexBuilder.vertex(matrix, 0.5f, -0.5f, 0)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();

            vertexBuilder.vertex(matrix, 0.5f, 0.5f, 0)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();

            vertexBuilder.vertex(matrix, -0.5f, 0.5f, 0)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();

            matrixStack.popPose();
        }
    }
}
