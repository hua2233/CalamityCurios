package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMixin extends AbstractContainerMenu {
    @Shadow @Final public int[] costs;

    @Shadow @Final private RandomSource random;

    @Unique private IntList calamity$EnchantmentInfo;

    protected EnchantmentMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "TAIL"))
    private void init(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        if (!CalamityCap.notHasCalamity() && !inventory.player.level().isClientSide && inventory.player.Calamity$Player.calamityCap.isCursePlayer()) {
            calamity$EnchantmentInfo = new IntArrayList();
            calamity$EnchantmentInfo.add(inventory.player.Calamity$Player.calamityCap.isInverted(CalamityCap.CurseType.DESERT) ? 1 : 0);
            ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(
                Enchantment::isCurse).mapToInt(BuiltInRegistries.ENCHANTMENT::getId).forEach(calamity$EnchantmentInfo::add);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER))
    public void modifyDisplayEnchantment(Container inventory, CallbackInfo ci) {
        //After the actual value and enchantment settings are completed, before the actual transmission,
        //the enchantment type and Xp consumption are corrected by whether there is a curse and whether it is reversed
        if (calamity$EnchantmentInfo != null) {
            float amplifier = calamity$EnchantmentInfo.getInt(0) == 1 ? 0.25f : 2f;
            for (int i = 0; i < 3; i++) {
                int v = costs[i];
                if (v > 0) costs[i] = (int) (costs[i] * amplifier);
            }

            broadcastChanges();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getEnchantmentList", at = @At("RETURN"))
    public void modifyApplyEnchantment(ItemStack stack, int enchantSlot, int level, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (calamity$EnchantmentInfo != null) {
            List<EnchantmentInstance> list = cir.getReturnValue();
            if (calamity$EnchantmentInfo.getInt(0) == 1) {
                for (int i = list.size() - 1; i >= 0 ; i--)
                    if (list.get(i).enchantment.isCurse()) list.remove(i);
            } else if (calamity$EnchantmentInfo.size() > 1) {
                Enchantment enchantment = Enchantment.byId(calamity$EnchantmentInfo.getInt(random.nextInt(1, calamity$EnchantmentInfo.size())));
                list.set(list.size() - 1, new EnchantmentInstance(enchantment, enchantment.getMaxLevel()));
            }
        }
    }
}
