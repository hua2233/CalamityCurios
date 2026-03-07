package hua223.calamity.register.Items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NoGravityItem extends TooltipItem {
    public NoGravityItem(Properties properties, String name, int lien) {
        super(properties, name, lien);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        location.setNoGravity(true);
        return null;
    }
}
