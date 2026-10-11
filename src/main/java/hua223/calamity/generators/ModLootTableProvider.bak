package hua223.calamity.generators;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(), ImmutableList.of(
            new SubProviderEntry(() -> new CalamityEntityLootTables(FeatureFlags.REGISTRY.allFlags()), LootContextParamSets.ENTITY),
            new SubProviderEntry(() -> new ModBlockLootTables(Set.of(), FeatureFlags.REGISTRY.allFlags()), LootContextParamSets.BLOCK)
        ));
    }

    @Override
    protected void validate(@NotNull Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationTracker) {
    }

    public static class CalamityEntityLootTables extends EntityLootSubProvider {
        protected CalamityEntityLootTables(FeatureFlagSet enabledFeatures) {
            super(enabledFeatures);
        }

        @Override
        public void generate() {

        }
    }

    private static class ModBlockLootTables extends BlockLootSubProvider {
        public static final List<Block> DROP_SELF_BLOCK = new ArrayList<>();
        public static final List<Block> CUSTOM_LOOTS = new ArrayList<>();

        protected ModBlockLootTables(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures) {
            super(explosionResistant, enabledFeatures);
        }

        public static void setLoots() {
            Collections.addAll(DROP_SELF_BLOCK);

            Collections.addAll(CUSTOM_LOOTS);
        }

        private static LootTable.Builder manyLoot(int[] weight, Item... loot) {
            int size = weight.length;
            LootTable.Builder table = LootTable.lootTable();
            if (size != loot.length) return table;
            LootPool.Builder lootPool = LootPool.lootPool();
            for (int i = 0; i < size; i++) {
                lootPool.add(LootItem.lootTableItem(loot[i]).setWeight(weight[i]));
            }
            return table.withPool(lootPool);
        }

        private static int[] getWeightArray(int... arr) {
            return arr;
        }

        protected LootTable.Builder createOreDrops(Block block, Item loot, float[] count) {
            return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(loot)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(count[0], count[1])))
                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        }


        @Override
        protected void generate() {
            for (Block block : DROP_SELF_BLOCK) {
                dropSelf(block);
            }
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return new ArrayList<>();
        }
    }
}
