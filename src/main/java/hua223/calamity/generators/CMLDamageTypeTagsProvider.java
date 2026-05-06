package hua223.calamity.generators;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CMLDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public CMLDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, future, CalamityCurios.MODID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        for (CalamityDamageTypes types : CalamityDamageTypes.values())
            if (types.tags != null)
                for (TagKey<DamageType> typeTagsTagKey : types.tags)  {
                    tag(typeTagsTagKey).replace(false).add(types.type);
                }
    }
}
