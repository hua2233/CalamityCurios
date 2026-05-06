package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.JewelSpike;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;

public class ScuttlersJewel extends BaseCurio {
    public ScuttlersJewel(Properties properties) {
        super(properties);
    }

    @ApplyEvent(500)
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(listener.player).resolve();
            if (optional.isEmpty()) return;
            ICuriosItemHandler handler = optional.get();
            Optional<SlotResult> slotResult = handler.findFirstCurio(this);
            if (slotResult.isPresent()) {
                SlotContext context = slotResult.get().slotContext();
                //This just requires a player to uninstall the context of this curio
                //Execute at the end of the tick to prevent Concurrent ModifierException
                DelayRunnable.addRunTask(0, () -> onUnequip(context, null, null));
                handler.setEquippedCurio(context.identifier(), context.index(), ItemStack.EMPTY);
                int i = listener.player.getRandom().nextInt(1, 4);
                Item gem = switch (i) {
                    case 1 -> Items.DIAMOND;
                    case 2 -> Items.GOLD_INGOT;
                    case 3 -> Items.IRON_INGOT;
                    default -> null;
                };

                listener.player.getInventory().add(new ItemStack(gem, i));
                JewelSpike.create(listener.player, listener.entity);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "scuttlers_jewel", 1, 2);
        return tooltips;
    }
}
