package hua223.calamity.generators.tag;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static hua223.calamity.main.CalamityCurios.MODID;
import static net.minecraftforge.common.Tags.Blocks.NEEDS_NETHERITE_TOOL;
import static net.minecraftforge.common.Tags.Blocks.NEEDS_WOOD_TOOL;

public class ModBlockTag extends BlockTagsProvider {
    public static final ArrayList<Block> EXPECTATIONS_PICKAXE_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> EXPECTATIONS_IRON_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> EXPECTATIONS_STONE_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> EXPECTATIONS_DIAMOND_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> EXPECTATIONS_NETHERITE_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> EXPECTATIONS_WOOD_TOOLS = new ArrayList<>(50);
    public static final ArrayList<Block> BEACON_BASE_BLOCKS = new ArrayList<>(20);

    public ModBlockTag(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(EXPECTATIONS_PICKAXE_TOOLS.toArray(Block[]::new));
        tag(BlockTags.NEEDS_IRON_TOOL).add(EXPECTATIONS_IRON_TOOLS.toArray(Block[]::new));
        tag(BlockTags.NEEDS_STONE_TOOL).add(EXPECTATIONS_STONE_TOOLS.toArray(Block[]::new));
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(EXPECTATIONS_DIAMOND_TOOLS.toArray(Block[]::new));
        tag(NEEDS_NETHERITE_TOOL).add(EXPECTATIONS_NETHERITE_TOOLS.toArray(Block[]::new));
        tag(NEEDS_WOOD_TOOL).add(EXPECTATIONS_WOOD_TOOLS.toArray(Block[]::new));
        tag(BlockTags.BEACON_BASE_BLOCKS).add(BEACON_BASE_BLOCKS.toArray(Block[]::new));
    }

}
