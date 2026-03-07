package hua223.calamity.generators;

import com.google.common.collect.Lists;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static hua223.calamity.main.CalamityCurios.MODID;

@OnlyIn(Dist.CLIENT)
public class ModItemModelGen extends ItemModelProvider {
    public ModItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        final String ITEM_GENERATED = "item/generated";
        final String ITEM_SWORD = "item/handheld";
        final String LAYER0 = "layer0";
        final StringBuilder builder = new StringBuilder("item/");

        List<CalamityItems> swordItem = Lists.newArrayList(CalamityItems.ZENITH,
            CalamityItems.CRYSTYL_CRUSHER);

        for (CalamityItems item : swordItem) {
            String name = item.getId().getPath();
            withExistingParent(name, ITEM_SWORD).texture(LAYER0,
                CalamityCurios.ModResource(builder.append(name).toString()));
            builder.setLength(5);
        }

        Set<CalamityItems> items = EnumSet.allOf(CalamityItems.class);
        getCustomizeExclusionItems(swordItem).forEach(items::remove);

        for (CalamityItems item : items) {
            String name = item.getId().getPath();
            withExistingParent(name, ITEM_GENERATED).texture(LAYER0,
                CalamityCurios.ModResource(builder.append(name).toString()));
            builder.setLength(5);
        }
    }

    static List<CalamityItems> getCustomizeExclusionItems(List<CalamityItems> preExclusion) {
         Collections.addAll(preExclusion, CalamityItems.CALAMITY, CalamityItems.SHATTERED_COMMUNITY,
             CalamityItems.EXCELSUS, CalamityItems.ATARAXIA, CalamityItems.NEBULOUS_CATACLYSM,
             CalamityItems.YHARIMS_CRYSTAL);
         return preExclusion;
    }

    //I don't seem to have any blocks
//    private void BlockGenerateModel(String name, ResourceLocation texture) {
//        withExistingParent(name, texture);
//    }
//
//    private ResourceLocation resourceBlock(String path) {
//        return CalamityCurios.ModResource("block/" + path);
//    }
}
