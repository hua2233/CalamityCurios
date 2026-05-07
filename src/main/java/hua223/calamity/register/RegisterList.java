package hua223.calamity.register;

import hua223.calamity.integration.curios.Decks;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.enchantments.DemonShadeBless;
import hua223.calamity.register.enchantments.SharedPain;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.register.gui.CalamityCurseMenu;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import hua223.calamity.register.recipe.CalamityCurseSerializer;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static hua223.calamity.main.CalamityCurios.MODID;
import static hua223.calamity.register.Items.CalamityItems.YHARIM_GIFT;
import static hua223.calamity.register.Items.CalamityItems.ZENITH;

public class RegisterList {
//    public static final TagKey<Item> WING = ItemTags.create(CalamityCurios.ModResource("wing"));
//    public static final TagKey<Item> TREADS = ItemTags.create(CalamityCurios.ModResource("treads"));
//    public static final TagKey<Item> SPRINT = ItemTags.create(CalamityCurios.ModResource("sprint"));
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
    public static RegistryObject<MenuType<CalamityCurseMenu>> CALAMITY_CURES;
    public static RegistryObject<Enchantment> SHARED_PAIN;
    public static RegistryObject<Enchantment> DEMON_SHADE_BLESS;
    public static Item.Properties CURIOS_COMMON = new Item.Properties().stacksTo(1);
    public static final Rarity CALAMITY = Rarity.create("CALAMITY", ChatFormatting.DARK_RED);
    public static Item.Properties CURIOS_UNCOMMON = new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON);
    public static Item.Properties CURIOS_RARE = new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
    public static Item.Properties CURIOS_EPIC = new Item.Properties().stacksTo(1).rarity(Rarity.EPIC);
    public static Item.Properties CURIOS_CALAMITY = new Item.Properties().stacksTo(1).rarity(CALAMITY);
    public static Item.Properties ITEM_COMMON = new Item.Properties();
    public static Item.Properties ITEM_UNCOMMON = new Item.Properties().rarity(Rarity.UNCOMMON);
    public static Item.Properties UNCOMMON_ONE = new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1);
    public static Item.Properties ITEM_RARE = new Item.Properties().rarity(Rarity.RARE);
    public static Item.Properties RARE_ONE = new Item.Properties().rarity(Rarity.RARE).stacksTo(1);
    public static Item.Properties ITEM_EPIC = new Item.Properties().rarity(Rarity.EPIC);
    public static Item.Properties EPIC_ONE = new Item.Properties().rarity(Rarity.EPIC).stacksTo(1);
    public static Item.Properties ITEM_CALAMITY = new Item.Properties().stacksTo(1).rarity(CALAMITY);

    public static int curiosIndex;
    public static boolean fillCurios = true;
    private static Item.Properties properties;

    public static void setUniqueProperties(@NotNull Item.Properties properties) {
        RegisterList.properties = properties;
    }

    public static Item.Properties getFoodProperties(Rarity rarity, int count, FoodProperties properties) {
        return new Item.Properties().rarity(rarity).stacksTo(count).food(properties);
    }
    
    public static Item.Properties getPotionProperties(Rarity rarity) {
        return new Item.Properties().rarity(rarity).stacksTo(16);
    }

    @Nullable
    public static Item.Properties getUniqueSettings() {
        Item.Properties p = properties;
        properties = null;
        return p;
    }

    public static final ForgeTier MOON = new ForgeTier(5, 3092, 10f, 7f,
        50, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static final ForgeTier GOD_EATER = new ForgeTier(6, 5174, 11f, 10f,
        80, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static final ForgeTier DRAGON = new ForgeTier(7, 7212, 12f, 15f,
        100, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static final ForgeTier SUPREME = new ForgeTier(9999, -1, 9999f, 20f,
        9999, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);

    public static final ArmorMaterial DEMON_SHADE = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(@NotNull ArmorItem.Type type) {
            return switch (type) {
                case HELMET, BOOTS -> 4024;
                case CHESTPLATE -> 5892;
                case LEGGINGS -> 4965;
            };
        }

        @Override
        public int getDefenseForType(@NotNull ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> 15;
                case CHESTPLATE -> 30;
                case LEGGINGS -> 25;
                case BOOTS -> 10;
            };
        }

        @Override
        public int getEnchantmentValue() {
            return 50;
        }

        @Override
        public @NotNull SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_NETHERITE;
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(Items.NETHERITE_INGOT);
        }

        @Override
        public @NotNull String getName() {
            return "demon_shade";
        }

        @Override
        public float getToughness() {
            return 8;
        }

        @Override
        public float getKnockbackResistance() {
            return 10;
        }
    };

    public static void build(IEventBus bus) {
        CalamityItems.register(bus);
        CalamityEntity.register(bus);
        CalamitySounds.register(bus);
        CalamityEffects.register(bus);
        CalamityAttributes.build(bus);
        CalamityLightBlock.registerBlock(bus);

        DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
        RECIPE_TYPE.register("calamity_curse", () -> CalamityCurseRecipe.CurseRecipeType.INSTANCE);
        RECIPE_TYPE.register(bus);
        DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
        RECIPE_SERIALIZER.register("calamity_curse", () -> CalamityCurseSerializer.INSTANCE);
        RECIPE_SERIALIZER.register(bus);

        DeferredRegister<MenuType<?>> CALAMITY_ENCHANTMENT = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
        CALAMITY_CURES = CALAMITY_ENCHANTMENT.register("calamity_curse_enchantment", () -> IForgeMenuType.create(CalamityCurseMenu::new));
        CALAMITY_ENCHANTMENT.register(bus);

        DeferredRegister<Enchantment> ENCHANTMENT = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);
        SHARED_PAIN = ENCHANTMENT.register("shared_pain", SharedPain::new);
        DEMON_SHADE_BLESS = ENCHANTMENT.register("demon_shade_bless", DemonShadeBless::new);
        ENCHANTMENT.register(bus);

        DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
        TABS.register("calamity_item", () -> CreativeModeTab
            .builder().icon(() -> ZENITH.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.calamity_item").withStyle(ChatFormatting.DARK_RED))
            .displayItems(((parameters, output) -> {
                CalamityItems[] items = CalamityItems.values();
                for (int i = curiosIndex; i < items.length; i++)
                    output.accept(items[i].get().getDefaultInstance());
            })).build());

        TABS.register("calamity_curios", () -> CreativeModeTab
            .builder().icon(() -> YHARIM_GIFT.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.calamity_curios").withStyle(ChatFormatting.DARK_RED))
            .displayItems((parameters, output) -> {
                CalamityItems[] items = CalamityItems.values();
                for (int i = 0; i < curiosIndex; i++) {
                    Item item = items[i].get();
                    if (item instanceof Decks decks) decks.fillItemCategory(output);
                    else output.accept(items[i].get().getDefaultInstance());
                }
            }).build());

        TABS.register(bus);
    }

    public static void onFMLSetUp() {
        ResourceLocation moon = CalamityCurios.ModResource("moon");
        ResourceLocation godEater = CalamityCurios.ModResource("god_eater");
        ResourceLocation dragon = CalamityCurios.ModResource("dragon");
        ResourceLocation supreme = CalamityCurios.ModResource("supreme");
        TierSortingRegistry.registerTier(MOON, moon, List.of(Tiers.NETHERITE), List.of(godEater));
        TierSortingRegistry.registerTier(GOD_EATER, godEater, List.of(moon), List.of(dragon));
        TierSortingRegistry.registerTier(DRAGON, dragon, List.of(godEater), List.of(supreme));
        TierSortingRegistry.registerTier(SUPREME, supreme, Arrays.asList(TierSortingRegistry.getSortedTiers().toArray()), List.of());

        ITEMS = null;
        SOUND_EVENTS = null;
        ENTITIES = null;
        EFFECTS = null;
        ATTRIBUTES = null;
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
    }
}
