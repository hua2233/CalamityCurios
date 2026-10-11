package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;
import java.util.UUID;

//Developer Gift
public abstract class Mask extends BaseCurio {
    public Mask() {
        super(RegisterList.CALAMITY_ONE);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        CuriosApi.addSlotModifier(modifier, "head", uuid, 1, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnderMask(SlotContext slotContext, EnderMan enderMan, ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("mask").withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        return tooltips;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render implements ICurioRenderer {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack item, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
            poseStack.pushPose();
            HumanoidModel<Player> model = (HumanoidModel<Player>) renderLayerParent.getModel();
            model.head.translateAndRotate(poseStack);
            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            Vector3f fullBrightNormal = new Vector3f(0, 1, 0);
            boolean avatar = item.getItem() == CalamityItems.AVATAR_MASK.get();
            ResourceLocation mask = CalamityCurios.ModResource(avatar ? "textures/misc/avatar.png" : "textures/item/deity_mask.png");
            float z = -4.01f / 16f;
            builderVertex(buffer.getBuffer(CalamityShaders.entityCutout(mask)), pose, normal, fullBrightNormal, z, light);
            if (avatar) {
                fullBrightNormal.mul(normal.transpose());
                normal.transpose();
                builderVertex(buffer.getBuffer(CalamityShaders.getGlowRenderType(
                    CalamityCurios.ModResource("textures/misc/avatar_glow.png"))), pose, normal, fullBrightNormal, z, LightTexture.FULL_BRIGHT);
            }
            poseStack.popPose();
        }

        private void builderVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, Vector3f normalVec, float z, int light) {
            consumer.vertex(pose, 0.25f, -0.5f, z).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
            consumer.vertex(pose, -0.25f, -0.5f, z).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
            consumer.vertex(pose, -0.25f, 0f, z).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
            consumer.vertex(pose, 0.25f, 0f, z).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
        }
    }
}
