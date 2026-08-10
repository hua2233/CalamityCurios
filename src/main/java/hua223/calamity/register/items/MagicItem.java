package hua223.calamity.register.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MagicItem extends AvailableItem {
    private final byte level;

    public MagicItem(Properties properties, int level) {
        super(properties);
        this.level = (byte) level;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !level.isClientSide
            && entity.calamity$Player.Calamity$Player.tryUseMagicItem(this.level))
            stack.shrink(1);
        return stack;
    }
}
