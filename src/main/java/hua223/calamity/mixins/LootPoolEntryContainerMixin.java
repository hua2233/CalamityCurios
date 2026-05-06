package hua223.calamity.mixins;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.*;

import java.util.function.Predicate;

@Mixin(LootPoolEntryContainer.class)
public class LootPoolEntryContainerMixin {
    @Shadow @Final private Predicate<LootContext> compositeCondition;
    @Unique
    public boolean calamity$SetAbsoluteOperation;

    /**
     * @author hua223
     * @reason Rewrite its decision function so that it can be executed as necessary
     */
    @Overwrite
    protected final boolean canRun(LootContext context) {
        return calamity$SetAbsoluteOperation || compositeCondition.test(context);
    }
}
