package hua223.calamity.generators.tag;

import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import static hua223.calamity.main.CalamityCurios.MODID;

public class ModItemTag extends ItemTagsProvider {
    public ModItemTag(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, new BlockTagsProvider(generator, MODID, existingFileHelper), MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(Tags.Items.MUSHROOMS).add(CalamityItems.ODD_MUSHROOM.get());
//        tag(RegisterList.TREADS)
//            .add(CalamityItems.ANGEL_TREADS.get());
//
//        tag(RegisterList.SPRINT)
//            .add(CalamityItems.ASGARD_VALOR.get())
//            .add(CalamityItems.DEEP_DIVER.get())
//            .add(CalamityItems.EVASION_SCARF.get())
//            .add(CalamityItems.ORNATE_SHIELD.get())
//            .add(CalamityItems.COUNTER_SCARF.get())
//            .add(CalamityItems.SHIELD_OF_THE_HIGH_RULER.get())
//            .add(CalamityItems.ELYSIAN_AEGIS.get())
//            .add(CalamityItems.STATIS_NINJA_BELT.get());
    }
}
