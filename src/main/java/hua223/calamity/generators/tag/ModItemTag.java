package hua223.calamity.generators.tag;

import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static hua223.calamity.main.CalamityCurios.MODID;

public class ModItemTag extends ItemTagsProvider {
    public ModItemTag(DataGenerator generator, CompletableFuture<HolderLookup.Provider> future,
                      CompletableFuture<TagsProvider.TagLookup<Item>> items,
                      CompletableFuture<TagsProvider.TagLookup<Block>> blocks, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), future, items, blocks, MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.Items.MUSHROOMS).add(CalamityItems.ODD_MUSHROOM.get());
    }
}
