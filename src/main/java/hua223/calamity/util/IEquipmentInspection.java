package hua223.calamity.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IEquipmentInspection {
    void onEquip(Player player);

    void onUnEquip(Player player);

    boolean isEffectiveSlot(EquipmentSlot slot);
}
