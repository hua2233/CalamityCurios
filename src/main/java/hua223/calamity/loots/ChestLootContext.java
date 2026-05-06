package hua223.calamity.loots;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

@BaseLootContextPacker.GlobalLootType(GlobalLoot.CHESTS_LOOTS)
public class ChestLootContext extends BaseLootContextPacker {
    public final String idString;
    public final ServerPlayer player;

    public ChestLootContext(ObjectArrayList<ItemStack> generatedLoot, LootContext context, ServerPlayer player, RandomSource source) {
        super(generatedLoot, context, source);
        this.player = player;
        idString = context.getQueriedLootTableId().getPath().substring(7);
    }

    public boolean fromSpecificName(String id) {
        return id.equals(idString);
    }

    public boolean fromFuzzyType(String type) {
        return idString.contains(type);
    }
}
