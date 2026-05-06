package hua223.calamity.net.C2SPacket;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.register.gui.CalamityCurseMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;

public class CurseEnchantmentPack extends C2S {
    public CurseEnchantmentPack() {
    }

    public CurseEnchantmentPack(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof CalamityCurseMenu menu && menu.isEffectiveSlot()) {
            Map<Integer, Integer> slotChange = menu.synthesis();
            if (slotChange != null) {
                ItemStack item = menu.getCurseSlotItem();
                Inventory inventory = player.getInventory();

                boolean hasResult = false;
                if (menu.isExhumed) {
                    int c = item.getCount();

                    hasResult = true;
                    int d = c - menu.reactantCount;
                    if (d > 0) {
                        item.setCount(d);
                        inventory.add(item);
                    }
                } else {
                    item.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment ->
                        enchantment.setRunes(menu.type));
                    menu.setShareRenderTag(item, menu.type.name());
                }

                for (Map.Entry<Integer, Integer> entry : slotChange.entrySet()) {
                    int slot = entry.getKey();
                    int count = entry.getValue();
                    if (count == 0) {
                        inventory.setItem(slot, ItemStack.EMPTY);
                    } else {
                        inventory.getItem(slot).setCount(count);
                    }
                }

                ItemStack stack = hasResult ? menu.result : item.copy();
                menu.setCurseItemChanged(stack);
            }
        }
    }
}
