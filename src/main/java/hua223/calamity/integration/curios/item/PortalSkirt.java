package hua223.calamity.integration.curios.item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.TrackedListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.RegisterList;
import hua223.calamity.render.font.CalamityFont;
import hua223.calamity.render.item.IAvatarBackgroundRender;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.item.PortalSkirtRenderer;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.function.Consumer;

//Developer Gift
public class PortalSkirt extends BaseCurio implements IDataPackResponse, IAvatarBackgroundRender {
    public PortalSkirt() {
        super(RegisterList.CALAMITY_ONE);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        writeFlagAndSendToAllTracking(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        writeFlagAndSendToAllTracking(player, false);
    }

    @ApplyEvent(20)
    public final void onDeath(DeathListener listener) {
        if (listener.canceledPlayerDeathIfNotCooldowns(this, .25f, 100)) {
            CalamitySounds.RIFT_OPEN.playSound(listener.player);
            Vec3 vec3 = listener.player.position();
            ServerLevel level = listener.player.serverLevel();
            IceSnowMirror.toSpawnPoint(listener.player);
            if (listener.player.serverLevel() != level || listener.player.distanceToSqr(vec3) > 64) CalamitySounds.RIFT_OPEN.playSound(listener.player);
            DeathListener.sendAnimatePack(null, listener.player, this, 0x000000, 0x07020A, 0x49215B, 0x9D012C, 0xDB1019);
        }
    }

    @ApplyEvent
    public final void onTracked(TrackedListener listener) {
        CompoundTag tag = getPack();
        tag.putBoolean("flag", true);
        listener.sendTrackedToTheSelf(tag, this);
    }

    @Override
    public @NotNull ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(CalamitySounds.RIFT_OPEN.get(), 1.0F, 1.0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return CalamityFont.getAvatar(Component.translatable(getDescriptionId(stack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new PortalSkirtRenderer();
            }
        });
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        if (tag.contains("flag")) {
           AbstractClientPlayer player = TrackedListener.getInitiator(tag);
           if (player != null) player.Calamity$Player.disableLegRendering = tag.getBoolean("flag");
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            int[] totem = tag.getIntArray("totem");
            ClientLevel level = minecraft.level;
            Entity entity = level.getEntity(totem[0]);
            if (entity != null) {
                DeathListener.spawnParticle(minecraft, entity, totem);
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), CalamitySounds.RIFT_OPEN.get(),
                    entity.getSoundSource(), 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("portal_skirt", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.getTranslatable("portal_skirt", 3).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("portal_skirt", 2).withStyle(ChatFormatting.DARK_RED));
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
            ICurioRenderer.translateIfSneaking(poseStack, slotContext.entity());
            //Is there really a difference in the length of the left and right legs of a model ..
            poseStack.translate(0, Math.max(model.leftLeg.y, model.rightLeg.y) * 0.0625f, 0);
            PortalSkirtRenderer.renderPortalSkirt(poseStack, buffer, 2f, true);
            poseStack.popPose();
        }
    }
}
