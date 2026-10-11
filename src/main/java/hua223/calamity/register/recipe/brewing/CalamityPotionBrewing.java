package hua223.calamity.register.recipe.brewing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class CalamityPotionBrewing {
    private static final List<IBrewingAble> POTION_LIST = new ObjectArrayList<>();

    private CalamityPotionBrewing() {}

    public static boolean isCalamityIngredient(ItemStack stack) {
        return matchingSubstances(null, stack) != null;
    }

    public static boolean hasCalamityMix(ItemStack input, ItemStack reagent) {
        return matchingSubstances(input, reagent) != null;
    }

    public static ItemStack tryCraft(ItemStack reagent, ItemStack potion) {
        IBrewingAble potions = matchingSubstances(potion, reagent);
        return potions != null ? potions.getResult() : potion;
    }

    private static IBrewingAble matchingSubstances(ItemStack input, ItemStack ingredient) {
        for (IBrewingAble potions : POTION_LIST)
            if (potions.matchingInput(input) && potions.matchingIngredient(ingredient)) return potions;

        return null;
    }

    public static void addPotionBrewing(IBrewingAble brewingAble) {
        POTION_LIST.add(brewingAble);
    }
}
