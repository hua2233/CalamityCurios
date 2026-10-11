package hua223.calamity.register.recipe.brewing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public interface IBrewingAble {
    default Potion getPotion() {
        return Potions.AWKWARD;
    }

    default boolean matchingInput(ItemStack input) {
        //禁用喷溅以及滞留的合成
        Potion potion = input == null ? null : PotionUtils.getPotion(input);
        if (potion != null && input.getItem() != Items.POTION) return false;

        return potion == null || getPotion() == potion;
    }

    default boolean matchingIngredient(ItemStack ingredient) {
        return getInput() == ingredient.getItem();
    }

    default Item getInput() {
        return Items.AIR;
    }

    default ItemStack getResult() {
        return ((Item) this).getDefaultInstance();
    }
}
