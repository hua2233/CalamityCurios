package hua223.calamity.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IArmorSetInspection {
    default void onEquipArmor(ItemStack stack, EquipmentSlot slot, Player player) {};
    default void onUnEquipArmor(ItemStack stack, EquipmentSlot slot, Player player) {};
}
