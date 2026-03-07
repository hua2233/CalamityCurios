package hua223.calamity.register.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CalamityCurseRecipe implements Recipe<SimpleContainer> {
    private final ItemStack result;
    private final NonNullList<Ingredient> inputs;
    private final ResourceLocation id;
    private final ItemStack reactant;

    public CalamityCurseRecipe(ItemStack reactant, ItemStack result, NonNullList<Ingredient> inputs, ResourceLocation id) {
        this.inputs = inputs;
        this.result = result;
        this.id = id;
        this.reactant = reactant;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer container) {
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CalamityCurseSerializer.INSTANCE;
    }

    @Override
    public RecipeType<CalamityCurseRecipe> getType() {
        return CurseRecipeType.INSTANCE;
    }

    public CalamityCurseRecipe returnIfMatching(ItemStack stack) {
        if (stack.is(reactant.getItem())) return this;
        return null;
    }

    public ItemStack getReactant() {
        return reactant;
    }

    public static class CurseRecipeType implements RecipeType<CalamityCurseRecipe> {
        public static final CurseRecipeType INSTANCE = new CurseRecipeType();
        public static final String TYPE = "calamity_curse";

        private CurseRecipeType() {
        }
    }
}
