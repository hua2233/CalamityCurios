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

public class AdrenalineItem extends AvailableItem {
    private final int id;

    public AdrenalineItem(Properties properties, int id) {
        super(properties);
        this.id = id;

    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(a -> {
                if (a.tryUseAdrenalineItem(id, player)) stack.shrink(1);
            });
        }
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltipComponents, TooltipFlag pIsAdvanced) {
        tooltipComponents.add(CMLangUtil.getTranslatable("adrenaline_item"));
    }
}
