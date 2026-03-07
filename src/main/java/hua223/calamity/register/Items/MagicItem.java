package hua223.calamity.register.Items;

import hua223.calamity.capability.CalamityCapProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicItem extends AvailableItem {
    private final String tag;
    private final byte level;

    public MagicItem(Properties properties, int level, String tag) {
        super(properties);
        this.tag = tag;
        this.level = (byte) level;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !level.isClientSide) {
            CalamityCapProvider.safetyRunCalamityMagic(entity, expand -> {
                if (expand.calamity$TryUseMagicItem(this.level, 50, tag)) stack.shrink(1);
            });
        }
        return stack;
    }
}
