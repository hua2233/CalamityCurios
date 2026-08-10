package hua223.calamity.util;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.projectiles.ItemPro;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public interface IThrowableItem {
    default void onHitBlock(ItemPro itemPro, BlockHitResult result) {}

    default void onHitEntity(ItemPro itemPro, EntityHitResult result) {}

    default boolean destroyAfterHitting(ItemPro itemPro) { return true; }

    @SuppressWarnings("ConstantConditions")
    default ItemPro of(ItemStack stack, Level level) {
        ItemPro pro = CalamityCurios.getEntityType(ItemPro.class).create(level);
        pro.setItem(stack);
        return pro;
    }

    default boolean customTick(ItemPro pro) {
        return false;
    }

    default float[] box() { return null; }

    @OnlyIn(Dist.CLIENT)
    default boolean customRender(ItemPro pro, ItemRenderer renderer, ItemStack stack, float partialTick,
                                 PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    default void onClientKill(ItemPro pro) {};

    @OnlyIn(Dist.CLIENT)
    default float[] scale() { return null; }

    @NotNull Component getProName();
}
