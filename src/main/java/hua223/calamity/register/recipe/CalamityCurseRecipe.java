package hua223.calamity.register.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@Nullable SimpleContainer container, @Nullable RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(@Nullable RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CalamityCurseSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<CalamityCurseRecipe> getType() {
        return CurseRecipeType.INSTANCE;
    }

    public boolean matching(ItemStack stack) {
        return stack.is(reactant.getItem());
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
