package hua223.calamity.register;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.register.gui.RegisterMenuType;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import hua223.calamity.register.recipe.CalamityCurseSerializer;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.tab.CreateTab.CALAMITY_CURIOS;
import static hua223.calamity.register.tab.CreateTab.CALAMITY_ITEM;

public class RegisterList {
//    public static final TagKey<Item> WING = ItemTags.create(CalamityCurios.ModResource("wing"));
//    public static final TagKey<Item> TREADS = ItemTags.create(CalamityCurios.ModResource("treads"));
//    public static final TagKey<Item> SPRINT = ItemTags.create(CalamityCurios.ModResource("sprint"));
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
    public static DeferredRegister<MenuType<?>> CALAMITY_ENCHANTMENT = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static Item.Properties CURIOS_COMMON = new Item.Properties().tab(CALAMITY_CURIOS).stacksTo(1);
    public static final Rarity CALAMITY = Rarity.create("CALAMITY", ChatFormatting.DARK_RED);
    public static Item.Properties CURIOS_UNCOMMON = new Item.Properties().tab(CALAMITY_CURIOS).stacksTo(1).rarity(Rarity.UNCOMMON);
    public static Item.Properties CURIOS_RARE = new Item.Properties().tab(CALAMITY_CURIOS).stacksTo(1).rarity(Rarity.RARE);
    public static Item.Properties CURIOS_EPIC = new Item.Properties().tab(CALAMITY_CURIOS).stacksTo(1).rarity(Rarity.EPIC);
    public static Item.Properties CURIOS_CALAMITY = new Item.Properties().tab(CALAMITY_CURIOS).stacksTo(1).rarity(CALAMITY);
    public static Item.Properties ITEM_COMMON = new Item.Properties().tab(CALAMITY_ITEM);
    public static Item.Properties ITEM_UNCOMMON = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.UNCOMMON);
    public static Item.Properties UNCOMMON_ONE = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.UNCOMMON).stacksTo(1);
    public static Item.Properties ITEM_RARE = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.RARE);
    public static Item.Properties RARE_ONE = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.RARE).stacksTo(1);
    public static Item.Properties ITEM_EPIC = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.EPIC);
    public static Item.Properties EPIC_ONE = new Item.Properties().tab(CALAMITY_ITEM).rarity(Rarity.EPIC).stacksTo(1);
    public static Item.Properties ITEM_CALAMITY = new Item.Properties().tab(CALAMITY_ITEM).stacksTo(1).rarity(CALAMITY);
    private static Item.Properties properties;
    public static void setUniqueProperties(@NotNull Item.Properties properties) {
        RegisterList.properties = properties;
    }

    public static Item.Properties getFoodProperties(Rarity rarity, int count, FoodProperties properties) {
        return new Item.Properties().tab(CALAMITY_ITEM).rarity(rarity).stacksTo(count).food(properties);
    }
    
    public static Item.Properties getPotionProperties(Rarity rarity) {
        return new Item.Properties().tab(CALAMITY_ITEM).rarity(rarity).stacksTo(16);
    }

    @Nullable
    public static Item.Properties getUniqueSettings() {
        Item.Properties p = properties;
        properties = null;
        return p;
    }

    public static final ForgeTier EXCELSUS = new ForgeTier(5, 11032, 11f, 5f,
        20, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static final ForgeTier CRYSTYL_TIER = new ForgeTier(9999, -1, 9999, 12f,
        9999, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static void build(IEventBus bus) {
        CalamityItems.register(bus);
        CalamityEntity.register(bus);
        CalamitySounds.register(bus);
        CalamityEffects.register(bus);
        CalamityAttributes.build(bus);
        CalamityLightBlock.registerBlock(bus);
        RegisterMenuType.build(bus);
        RECIPE_TYPE.register("calamity_curse", () -> CalamityCurseRecipe.CurseRecipeType.INSTANCE);
        RECIPE_TYPE.register(bus);
        RECIPE_SERIALIZER.register("calamity_curse", () -> CalamityCurseSerializer.INSTANCE);
        RECIPE_SERIALIZER.register(bus);
    }

    public static void registerTier() {
        TierSortingRegistry.registerTier(EXCELSUS, CalamityCurios.ModResource("excelsus"), List.of(Tiers.NETHERITE), List.of());

        List<Tier> tiers = TierSortingRegistry.getSortedTiers();
        TierSortingRegistry.registerTier(CRYSTYL_TIER, CalamityCurios.ModResource("supreme"),
            List.of(tiers.isEmpty() ? Tiers.NETHERITE : tiers.get(tiers.size() - 1)), List.of());
    }

    public static void clearRegisters() {
        ITEMS = null;
        SOUND_EVENTS = null;
        ENTITIES = null;
        EFFECTS = null;
        ATTRIBUTES = null;
        CALAMITY_ENCHANTMENT = null;
        RECIPE_SERIALIZER = null;
        CURIOS_COMMON = null;
        CURIOS_UNCOMMON = null;
        CURIOS_RARE = null;
        CURIOS_EPIC = null;
        CURIOS_CALAMITY = null;
        ITEM_COMMON = null;
        ITEM_UNCOMMON = null;
        ITEM_RARE = null;
        ITEM_EPIC = null;
        ITEM_CALAMITY = null;
        RECIPE_TYPE = null;
    }
}
