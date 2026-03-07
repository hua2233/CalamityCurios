package hua223.calamity.register.Items;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RageItem extends AvailableItem {
    private final int id;
    private final int tick;

    public RageItem(Properties properties, int id, int tick) {
        super(properties);
        this.id = id;
        this.tick = tick;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            CalamityCapProvider.RAGE.getCapabilityFrom(entity).ifPresent(rage -> {
                if (rage.tryUseRageItem(tick, id, (ServerPlayer) entity)) {
                    stack.shrink(1);
                }
            });
        }
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        tooltips.add(CMLangUtil.getDynamic("rage_item", tick / 20));
    }
}
