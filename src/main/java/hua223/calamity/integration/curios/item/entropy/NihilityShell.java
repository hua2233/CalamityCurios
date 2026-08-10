package hua223.calamity.integration.curios.item.entropy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityPlayer;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class NihilityShell extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    public NihilityShell(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ItemCooldowns cooldowns = listener.player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) return;

        float[] count = getCount(listener.player);
        if (count[0] > 0) {
            cooldowns.addCooldown(this, 120);
            listener.amplifier -= count[0] * 0.25f;
            count[0] = 0;
            getPack().putByte("c", (byte) 0);
            sendToClient(listener.player);
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.applyAmplifier(0.12f);
        float[] count = getCount(listener.player);
        if (count[0] < 3) {
            CompoundTag tag = getPack();
            tag.putInt("id", listener.player.getId());
            tag.putByte("c", (byte) ++count[0]);
            sendToAllClient();
        }
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = getPack();
        tag.putInt("id", player.getId());
        tag.putByte("c", (byte) 0);
        sendToAllClient();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        ((AbstractClientPlayer) Minecraft.getInstance().level.getEntity(tag.getInt("id")))
            .Calamity$Player.nihilityShell = tag.getByte("c");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "nihility_shell", 1, 2);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("nihility_shell", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
        return super.getSlotsTooltip(tooltips, stack);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render implements ICurioRenderer {
        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack item, SlotContext slotContext, PoseStack stack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
            byte count = slotContext.entity().calamity$Player.Calamity$Player.nihilityShell;
            if (count > 0) {
                float age = slotContext.entity().tickCount + partialTicks;
                float rotateAngleY = age / 5.0F;
                stack.mulPose(Axis.XP.rotation(Mth.PI));
                for (int c = 0; c < count; c++) {
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    BakedModel model = itemRenderer.getModel(item, null, null, 1);
                    stack.pushPose();
                    stack.translate(-0.5, -0.6, -0.5);
                    stack.translate(0.5, 0.5, 0.5);
                    stack.mulPose(Axis.YP.rotationDegrees(rotateAngleY * (180F / (float) Math.PI) + (c * (360F / count))));
                    stack.translate(-0.5, -0.5, -0.5);
                    stack.translate(0F, 0F, -1F);
                    RenderUtil.renderItemModelList(itemRenderer, model, item, stack, buffer,15728880, OverlayTexture.NO_OVERLAY);
                    stack.popPose();
                }
            }
        }
    }
}
