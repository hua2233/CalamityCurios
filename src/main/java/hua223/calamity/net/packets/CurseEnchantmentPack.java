package hua223.calamity.net.packets;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.gui.CalamityCurseMenu;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class CurseEnchantmentPack extends DataPack {
    public CurseEnchantmentPack() {
    }

    public CurseEnchantmentPack(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof CalamityCurseMenu menu && menu.isEffectiveSlot()) {
            Int2IntMap slotChange = menu.synthesis();
            if (slotChange != null) {
                ItemStack item = menu.getCurseSlotItem();

                if (menu.isExhumed) {
                    int d = item.getCount() - menu.reactantCount;
                    if (d >= 0) {
                        if (d > 0) {
                            item.setCount(d);
                            player.getInventory().add(item);
                        }
                        item = menu.result;
                    } else return;
                } else {
                    item.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment ->
                        enchantment.setRunes(menu.type));
                    menu.setShareRenderTag(item);
                }

                for (Int2IntMap.Entry entry : slotChange.int2IntEntrySet()) {
                    Slot slot = menu.slots.get(entry.getIntKey());
                    int count = entry.getIntValue();
                    if (count == 0) slot.set(ItemStack.EMPTY);
                    else slot.getItem().setCount(count);

                    slot.setChanged();
                }

                menu.setCurseItemChanged(item);
            }
        }
    }
}
