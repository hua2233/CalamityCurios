package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdrenalineItem extends AvailableItem {
    private final int id;

    public AdrenalineItem(Properties properties, int id) {
        super(properties);
        this.id = id;

    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!entity.level().isClientSide && entity.calamity$IsPlayer &&
            entity.calamity$Player.Calamity$Player.adrenaline.tryUseAdrenalineItem(id))
            stack.shrink(1);
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltipComponents, TooltipFlag pIsAdvanced) {
        tooltipComponents.add(CMLangUtil.getTranslatable("adrenaline_item"));
    }
}
