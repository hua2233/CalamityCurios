package hua223.calamity.generators;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected @NotNull List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
            Pair.of(CalamityEntityLootTables::new, LootContextParamSets.ENTITY)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        //这仅仅是为了跳过原版验证，防止报错，使程序执行失败。（尽管这与mod几乎没有任何关系，Well, it's magical...）
    }

    public static class CalamityEntityLootTables extends EntityLoot {
        @Override
        protected void addTables() {
            add(EntityType.MOOSHROOM, LootTable.lootTable());
        }
    }

    private static class ModBlockLootTables extends BlockLoot {
        public static final List<Block> DROP_SELF_BLOCK = new ArrayList<>();
        public static final List<Block> CUSTOM_LOOTS = new ArrayList<>();

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

        protected static LootTable.Builder createOreDrops(Block block, Item loot, float[] count) {
            return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(loot)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(count[0], count[1])))
                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void addTables() {
            setLoots();
            // 应用矿石战利品
            //add(TOPAZ_ORE.get(), (Function)(block -> createOreDrop((Block)block, RegisterItem.TOPAZ_SHARD.get())));
            for (Block block : DROP_SELF_BLOCK) {
                dropSelf(block);
            }
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            // 返回你自定义的所有方块
            //List<Block> combined = new ArrayList<>(COMMON_MODLES_BLOCKS.size() + DROP_SELF_BLOCK.size() + CUSTOM_LOOTS.size());
//            combined.addAll(COMMON_MODLES_BLOCKS);
//            combined.addAll(DROP_SELF_BLOCK);
//            combined.addAll(CUSTOM_LOOTS);
//            return ImmutableList.copyOf(combined);
            return new ArrayList<>();
        }
    }
}
