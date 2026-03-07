package hua223.calamity.register.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class CalamityCurseSerializer implements RecipeSerializer<CalamityCurseRecipe> {
    public static final CalamityCurseSerializer INSTANCE = new CalamityCurseSerializer();
    public static final ResourceLocation NAME = CalamityCurios.ModResource("calamity_curse");

    private CalamityCurseSerializer() {
    }

        private static void parseRenderData(CalamityCurseRecipe recipe, JsonObject serializedRecipe) {
        JsonObject render = GsonHelper.getAsJsonObject(serializedRecipe, "renderer");
        ItemStack stack = recipe.getResultItem();

        if (GsonHelper.getAsBoolean(render, "isDefault")) {
            EnchantmentProvider.addDefaultProvider(stack);
        } else {
            EnchantmentProvider.addStyleProvider(stack.getItem(),
                GsonHelper.getAsBoolean(render, "gradual"),
                Integer.decode(GsonHelper.getAsString(render, "startColor")),
                Integer.decode(GsonHelper.getAsString(render, "endColor")),
                GsonHelper.getAsInt(render, "semiCycle"), true);
        }
    }

    @Override
    public CalamityCurseRecipe fromJson(ResourceLocation id, JsonObject serializedRecipe) {
        JsonArray array = GsonHelper.getAsJsonArray(serializedRecipe, "ingredients");
        int size = array.size();
        if (size > 5)
            throw new JsonSyntaxException("this ingredient is illegally: The maximum number of factors for an ingredient is 5");

        NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
        for (int i = 0; i < size; i++) {
            JsonElement element = array.get(i);
            if (element.isJsonObject()) {
                ingredients.set(i, Ingredient.of(ShapedRecipe.itemStackFromJson(element.getAsJsonObject())));
            } else
                throw new JsonSyntaxException("this ingredient is illegally: What is needed in the ingredient array is a json object");
        }

        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));
        ItemStack reactant = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "reactant"));

        CalamityCurseRecipe recipe = new CalamityCurseRecipe(reactant, result, ingredients, id);
        parseRenderData(recipe, serializedRecipe);
        return recipe;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @Nullable CalamityCurseRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        ItemStack result = buffer.readItem();
        ItemStack reactant = buffer.readItem();
        int var = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(var, Ingredient.EMPTY);

        for (int i = 0; i < var; i++) {
            ingredients.set(i, Ingredient.fromNetwork(buffer));
        }

        return new CalamityCurseRecipe(reactant, result, ingredients, id);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CalamityCurseRecipe recipe) {
        buffer.writeItemStack(recipe.getResultItem(), false);
        buffer.writeItemStack(recipe.getReactant(), false);
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        buffer.writeVarInt(ingredients.size());

        for (Ingredient ingredient : ingredients) {
            ingredient.toNetwork(buffer);
        }
    }
}
