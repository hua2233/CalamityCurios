package hua223.calamity.render.Item;

import com.google.common.collect.ImmutableMap;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import hua223.calamity.render.screen.particleset.EnchantedParticleSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.ItemDecoratorHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ExhumedDecoratorSystem {
    private static boolean init = true;
    public static final IItemDecorator EXHUMED_DECORATOR = (gui, font, itemStack, x, y) -> EnchantedParticleSet.drawSet(x, y, gui);

    @SuppressWarnings("all")
    public static void registerExhumedItemDecorator(RecipeManager manager) {
        //Error! Please do not do this in the Register Item Decorations Event, as the recipe does not exist on the client side at this time,
        //and do not attempt to obtain it locally, as the recipe should be synchronized from the server side
        //Minecraft.getBlackInstance().level.getRecipeManager().getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);

        //Register after migrating to recipes update event, as the client has already been synchronized at this time
        List<CalamityCurseRecipe> recipes = manager.getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);
        Map<Item, ItemDecoratorHandler> decoratorMap = ItemDecoratorHandler.DECORATOR_LOOKUP;
        if (recipes.isEmpty()) {
            ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
            ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(decoratorMap).build();
            return;
        }


        ItemDecoratorHandler handler = new ItemDecoratorHandler(List.of(EXHUMED_DECORATOR));
        for (CalamityCurseRecipe recipe : recipes) {
            Item item = recipe.getReactant().getItem();
            decoratorMap.compute(item, (k, v) -> {
                //Each item should have only one decorative renderer
                if (v == null) return handler;
                else if (!v.itemDecorators.contains(EXHUMED_DECORATOR))
                    v.itemDecorators.add(EXHUMED_DECORATOR);

                return v;
            });
        }

        //After the setting is completed, it will be reverted back to immutable
        ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
        ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(decoratorMap).build();
    }

    @LogoutRelease
    public static void onLogOut(LocalPlayer player) {
        clearOldDecorator(player.level().getRecipeManager(), true);
    }

    @SuppressWarnings("all")
    public static void clearOldDecorator(RecipeManager manager, boolean isLogout) {
        HashMap<Item, ItemDecoratorHandler> map = new HashMap<>(ItemDecoratorHandler.DECORATOR_LOOKUP);
        ItemDecoratorHandler.DECORATOR_LOOKUP = map;

        if (init) {
            //When entering the world and not yet initialized, the client has not accepted synchronization,
            //has no old values, and is only set as HashMap, providing a mutable container for future synchronization changes
            init = false;
            return;
        }

        for (CalamityCurseRecipe recipe : manager.getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE)) {
            Item reactant = recipe.getReactant().getItem();
            if (map.containsKey(reactant)) {
                List<IItemDecorator> decorators = map.get(reactant).itemDecorators;

                if (decorators.size() == 1) map.remove(reactant);
                else decorators.remove(EXHUMED_DECORATOR);
            }
        }

        if (isLogout) {
            //Restore initialization upon exit and set it to immutable
            //Client data is automatically reset only upon JVM restart,
            //so manually set to an immutable default value upon exit to prevent confusion of client data caused by entering multiple servers
            init = true;
            ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
            ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(map).build();
        }
    }
}
