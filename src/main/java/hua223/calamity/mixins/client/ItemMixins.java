package hua223.calamity.mixins.client;

import hua223.calamity.capability.EnchantmentProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixins {
    @Redirect(method = "fillItemCategory", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z")) //, remap = false
    private boolean addCurse(NonNullList<ItemStack> instance, Object o) {
        //某些常规物品在创造物品栏时的能力附加事件无法被触发。此处处理客户端的渲染。
        ItemStack stack = (ItemStack) o;
        if (!stack.hasTag() && EnchantmentProvider.isExhumed(stack))
            stack.getOrCreateTag().putString(EnchantmentProvider.FONT_FLAG, "EXHUMED");
        return instance.add(stack);
    }
}
