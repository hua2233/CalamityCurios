package hua223.calamity.register.Items;

import hua223.calamity.mixed.ICalamityMagicExpand;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicItem extends AvailableItem {
    private final byte level;

    public MagicItem(Properties properties, int level) {
        super(properties);
        this.level = (byte) level;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !level.isClientSide
            && ((ICalamityMagicExpand) MagicData.getPlayerMagicData(entity)).calamity$TryUseMagicItem(this.level, getDescription().getString()))
            stack.shrink(1);
        return stack;
    }
}
