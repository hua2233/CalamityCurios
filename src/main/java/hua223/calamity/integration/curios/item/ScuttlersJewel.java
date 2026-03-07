package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.JewelSpike;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

import java.util.List;
import java.util.Optional;

public class ScuttlersJewel extends BaseCurio {
    public ScuttlersJewel(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            DelayRunnable.nextTickRun(() -> {
                if (listener.player.isDeadOrDying()) return;
                ICuriosHelper helper = CuriosApi.getCuriosHelper();
                Optional<SlotResult> optional = helper.findFirstCurio(listener.player, this);
                if (optional.isPresent()) {
                    SlotContext context = optional.get().slotContext();
                    //This just requires a player to uninstall the context of this curio
                    onUnequip(context, null, null);
                    helper.setEquippedCurio(listener.player, context.identifier(), context.index(), ItemStack.EMPTY);
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
            });
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("scuttlers_jewel", 1));
        tooltips.add(CMLangUtil.getTranslatable("scuttlers_jewel", 2));
        return tooltips;
    }
}
