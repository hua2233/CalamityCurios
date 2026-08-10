package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hua223.calamity.register.entity.AutoEntityRegister;
import hua223.calamity.util.IThrowableItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

@AutoEntityRegister(sized = {.5f, .5f}, trackingRange = 4, updateInterval = 1)
public class ItemPro extends ThrowableItemProjectile {
    public ItemPro(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void setItem(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof IThrowableItem) super.setItem(stack);
        else throw new IllegalArgumentException();
    }

    @Override
    public void tick() {
        if (firstTick) refreshDimensions();

        if (getItem().getItem() instanceof IThrowableItem item) {
            if (item.customTick(this)) return;
        } else {
            discard();
            return;
        }

        super.tick();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        IThrowableItem item = (IThrowableItem) getItem().getItem();
        item.onHitBlock(this, result);
        if (item.destroyAfterHitting(this)) discard();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        float[] scale = ((IThrowableItem) getItem().getItem()).box();
        return scale == null ? getType().getDimensions() : EntityDimensions.scalable(scale[0], scale[1]);
    }

    @Override
    public @NotNull Component getName() {
       return ((IThrowableItem) getItem().getItem()).getProName();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (result.getEntity() != getOwner()) {
            IThrowableItem item = (IThrowableItem) getItem().getItem();
            item.onHitEntity(this, result);
            if (item.destroyAfterHitting(this)) discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientRemoval() {
        super.onClientRemoval();
        if (getItem().getItem() instanceof IThrowableItem item) item.onClientKill(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<ItemPro> {
        private final ItemRenderer itemRenderer;

        public Renderer(EntityRendererProvider.Context context) {
            super(context);
            itemRenderer = context.getItemRenderer();
        }

        @Override
        protected int getBlockLightLevel(ItemPro entity, BlockPos pos) {
            return 15;
        }

        @Override
        public void render(ItemPro entity, float yaw, float pPartialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            ItemStack stack = entity.getItem();
            IThrowableItem item = (IThrowableItem) stack.getItem();
            if (item.customRender(entity, itemRenderer, stack, pPartialTick, pose, buffer, packedLight)) return;
            pose.pushPose();
            float[] scale = item.scale();
            if (scale != null) pose.scale(scale[0], scale[1], scale[2]);
            pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, pose, buffer, entity.level(), entity.getId());
            pose.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull ItemPro itemPro) {
            return InventoryMenu.BLOCK_ATLAS;
        }
    }
}
