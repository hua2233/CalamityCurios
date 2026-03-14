package hua223.calamity.mixins;

import hua223.calamity.capability.CalamityCap;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.phys.AABB;
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
    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final public int[] costs;

    @Shadow @Final public int[] enchantClue;

    @Shadow @Final private RandomSource random;

    @Shadow @Final public int[] levelClue;

    @Unique private int[] calamity$EnchantmentInfo;

    protected EnchantmentMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "TAIL"))
    private void init(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        calamity$EnchantmentInfo = new int[] {-1, -1, -1};
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER))
    public void modifyDisplayEnchantment(Container inventory, CallbackInfo ci) {
        //After the actual value and enchantment settings are completed, before the actual transmission,
        //the enchantment type and Xp consumption are corrected by whether there is a curse and whether it is reversed
        calamity$EnchantmentInfo[0] = -1;
        access.execute((level, pos) -> level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(10))
            .stream().filter(player -> player.containerMenu == this && CalamityCap.isCalamity(player))
            .findFirst().ifPresent(player -> {
                boolean reverse = CalamityCap.isInverted(CalamityCap.CurseType.DESERT, player);
                float amplifier = reverse ? 0.25f : 2f;
                for (int i = 0; i < 3; i++) {
                    int v = costs[i];
                    if (v > 0) {
                        calamity$EnchantmentInfo[0] = i;
                        costs[i] = (int) (costs[i] * amplifier);
                    }
                }

                if (calamity$EnchantmentInfo[0] != -1) {
                    calamity$EnchantmentInfo[2] = reverse ? 0 : 1;
                    if (reverse) {
                        for (int i = 0; i < calamity$EnchantmentInfo[0]; i++) {
                            Enchantment enchantment = Registry.ENCHANTMENT.byId(enchantClue[i]);
                            if (enchantment != null && enchantment.isCurse()) {
                                levelClue[i] = -1;
                                enchantClue[i] = -1;
                            }
                        }
                    } else {
                        List<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(Enchantment::isCurse).toList();
                        if (enchantments.isEmpty()) return;

                        Enchantment enchantment = enchantments.get(random.nextInt(0, enchantments.size()));
                        levelClue[calamity$EnchantmentInfo[0]] = enchantment.getMaxLevel();
                        enchantClue[calamity$EnchantmentInfo[0]] = Registry.ENCHANTMENT.getId(enchantment);
                    }

                    broadcastChanges();
                }
            }));
    }

    @Inject(method = "clickMenuButton", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V", shift = At.Shift.BEFORE))
    public void preModifyApply(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        calamity$EnchantmentInfo[1] = 1;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Inject(method = "getEnchantmentList", at = @At("RETURN"))
    public void modifyApplyEnchantment(ItemStack stack, int enchantSlot, int level, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (calamity$EnchantmentInfo[1] == 1) {
            calamity$EnchantmentInfo[1] = -1;
            if (calamity$EnchantmentInfo[0] != enchantSlot) return;

            List<EnchantmentInstance> list = cir.getReturnValue();
            if (calamity$EnchantmentInfo[0] > 0)
                for (int i = list.size() - 1; i >= 0 ; i--)
                   if (list.get(i).enchantment.isCurse()) list.remove(i);
            else list.set(list.size() - 1, new EnchantmentInstance(Registry.ENCHANTMENT.byId(
                    enchantClue[calamity$EnchantmentInfo[0]]), levelClue[calamity$EnchantmentInfo[0]]));

            calamity$EnchantmentInfo[0] = -1;
        }
    }
}
