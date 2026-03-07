package hua223.calamity.generators;

import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.loots.GlobalLootModifier;
import hua223.calamity.loots.LootTableTypeCondition;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

import static hua223.calamity.main.CalamityCurios.MODID;

public class GlobalLootsProvider extends GlobalLootModifierProvider {
    public GlobalLootsProvider(DataGenerator gen) {
        super(gen, MODID);
    }

    @Override
    protected void start() {
        add("chests_global_loots", new GlobalLootModifier(
            new LootItemCondition[]{LootTableTypeCondition.of("chests/")}, GlobalLoot.CHESTS_LOOTS));
        add("entities_global_loots", new GlobalLootModifier(
            new LootItemCondition[]{LootTableTypeCondition.of("entities/")}, GlobalLoot.ENTITY_LOOTS));
    }
}
