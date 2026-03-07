package hua223.calamity.register.Items;

import hua223.calamity.integration.curios.item.*;
import hua223.calamity.integration.curios.item.entropy.*;
import hua223.calamity.register.Items.edible.*;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static hua223.calamity.register.RegisterList.*;
import static hua223.calamity.register.tab.CreateTab.CALAMITY_ITEM;

public enum CalamityItems implements EnumRegister<Item>/*, IExtensibleEnum */ {
    //Curios
    YHARIM_GIFT("yharim_gift", () -> new YharimGift(CURIOS_UNCOMMON)),
    RADIANCE("radiance", () -> new Radiance(CURIOS_EPIC)),
    BLOODY_WORM_TOOTH("bloody_worm_tooth", () -> new BloodyWormTooth(CURIOS_COMMON)), //血蠕虫之牙
    BLOODY_WORM_SCARF("bloody_worm_scarf", () -> new BloodyWormScarf(CURIOS_COMMON)),
    COUNTER_SCARF("counter_scarf", () -> new CounterScarf(CURIOS_COMMON)),
    EVASION_SCARF("evasion_scarf", () -> new EvasionScarf(CURIOS_COMMON)),
    HIDE_OF_ASTRUM_DEUS("hide_of_astrum_deus", () -> new AstrumDeusHide(CURIOS_UNCOMMON)),
    WARBANNER_SUN("warbanner_of_the_sun", () -> new SunWarBanner(CURIOS_UNCOMMON)),
    BADGE_BRAVERY("badge_of_bravery", () -> new BraveryBadge(CURIOS_COMMON)),
    ELEMENTAL_GAUNTLET("elemental_gauntlet", () -> new ElementalGauntlet(CURIOS_EPIC)),
    NIHILITY_QUIVER("nihility_quiver", () -> new NihilityQuiver(CURIOS_EPIC)),
    RUSTY_MEDALLION("rusty_medallion", () -> new RustyMedallion(CURIOS_COMMON)),
    DEADSHOT_BROOCH("deadshot_brooch", () -> new DeadshotBrooch(CURIOS_UNCOMMON)),
    DAAWNLIGHT("daawnlight_spirit_origin", () -> new DaawnlightSpiritOrigin(CURIOS_RARE)),
    STEM_CELLS("dynamo_stem_cells", () -> new DynamoStemCells(CURIOS_UNCOMMON)),
    ELEMENTAL_QUIVER("elemental_quiver", () -> new ElementalQuiver(CURIOS_EPIC)),
    CONCOCTION("permafrost_concoction", () -> new PermafrostConcoction(CURIOS_RARE)),
    MANA_POLARIZER("mana_polarizer", () -> new ManaPolarizer(CURIOS_COMMON)),
    CHAOS_STONE("chaos_stone", () -> new ChaosStone(CURIOS_UNCOMMON)),
    CALAMITY_SIGIL("calamity_sigil", () -> new CalamitySigil(CURIOS_UNCOMMON)),
    ETHEREAL_TALISMAN("ethereal_talisman", () -> new EtherealTalisman(CURIOS_RARE)),
    CRAW_CARAPACE("craw_carapace", () -> new CrawCarapace(CURIOS_COMMON)),
    ABYSSAL_AMULET("abyssal_amulet", () -> new AbyssalAmulet(CURIOS_COMMON)),
    ROTTEN_BRAIN("rotten_brain", () -> new RottenBrain(CURIOS_UNCOMMON)),
    SHATTERED_COMMUNITY("shattered_community", () -> new ShatteredCommunity(CURIOS_EPIC)),
    DARKNESS_HEART("darkness_heart", () -> new DarknessHeart(CURIOS_EPIC)),
    ABADDON("abaddon", () -> new Abaddon(CURIOS_COMMON, 0.08)),
    EXTINCTION_VOID("extinction_void", () -> new ExtinctionVoid(CURIOS_UNCOMMON, 0.12)),
    CALAMITY_VOID("calamity_void", () -> new CalamityVoid(CURIOS_UNCOMMON)),
    VEXATION_NECKLACE("vexation_necklace", () -> new VexationNecklace(CURIOS_COMMON)),
    DESTINY_BOOK("destiny_book", () -> new DestinyBook(CURIOS_EPIC)),
    SAND_SHARK("sand_shark_tooth_necklace", () -> new ToothNecklace(CURIOS_UNCOMMON, 10, 0.06f)),
    REAPER("reaper_tooth_necklace", () -> new ToothNecklace(CURIOS_RARE, 15, 0.15f)),
    BAROCLAW("baroclaw", () -> new Baroclaw(CURIOS_COMMON)),
    ELDRITCH("eldritch_soul_artifact", () -> new EldritchSoulArtifact(CURIOS_EPIC)),
    AFFLICTION("affliction", () -> new Affliction(CURIOS_UNCOMMON)),
    DIMENSIONAL("dimensional_soul_artifact", () -> new DimensionalSoulArtifact(CURIOS_EPIC)),
    CALAMITY("calamity", () -> new Calamity(CURIOS_CALAMITY)),
    GIANT_SHELL("giant_shell", () -> new GiantShell(CURIOS_COMMON)),
    LIFE_JELLY("life_jelly", () -> new LifeJelly(CURIOS_COMMON)),
    CLEANSING_JELLY("cleansing_jelly", () -> new CleansingJelly(CURIOS_COMMON)),
    MARNITE("marnite_repulsion_shield", () -> new MarniteRepulsionShield(CURIOS_COMMON)),
    FUNGAL_SYMBIOTE("fungal_symbiote", () -> new FungalSymbiote(CURIOS_COMMON)),
    CROWN_JEWEL("crown_jewel", () -> new CrownJewel(CURIOS_COMMON)),
    OCEAN_SHIELD("ocean_shield", () -> new OceanShield(CURIOS_COMMON)),
    GIANT_PEARL("giant_pearl", () -> new GiantPearl(CURIOS_COMMON)),
    BEE("bee", () -> new TheBee(CURIOS_COMMON)),
    HONEY_DEW("honey_dew", () -> new HoneyDew(CURIOS_COMMON)),
    ARCHAIC_POWDER("archaic_powder", () -> new ArchaicPowder(CURIOS_COMMON)),
    RADIANT_OOZE("radiant_ooze", () -> new RadiantOoze(CURIOS_COMMON)),
    FROST_BARRIER("frost_barrier", () -> new FrostBarrier(CURIOS_COMMON)),
    URSA_SERGEANT("ursa_sergeant", () -> new UrsaSergeant(CURIOS_COMMON)),
    GIANT_TORTOISE_SHELL("giant_tortoise_shell", () -> new GiantTortoiseShell(CURIOS_UNCOMMON)),
    AMALGAMATED_BRAIN("amalgamated_brain", () -> new AmalgamatedBrain(CURIOS_UNCOMMON)),
    REGENATOR("regenator", () -> new Regenator(CURIOS_UNCOMMON)),
    BLOOM_STONE("bloom_stone", () -> new BloomStone(CURIOS_UNCOMMON)),
    LIVING_DEW("living_dew", () -> new LivingDew(CURIOS_UNCOMMON)),
    ANGELIC_ALLIANCE("angelic_alliance", () -> new AngelicAlliance(CURIOS_UNCOMMON)),
    INFECTED_JEWEL("infected_jewel", () -> new InfectedJewel(CURIOS_UNCOMMON)),
    BLOOD_PACT("blood_pact", () -> new BloodPact(CURIOS_UNCOMMON)),
    FLESH_TOTEM("flesh_totem", () -> new FleshTotem(CURIOS_UNCOMMON)),
    AMBROSIAL_AMPOULE("ambrosial_ampoule", () -> new AmbrosialAmpoule(CURIOS_RARE)),
    VITAL_JELLY("vital_jelly", () -> new VitalJelly(CURIOS_COMMON)),
    GRAND_GELATIN("grand_gelatin", () -> new GrandGelatin(CURIOS_UNCOMMON)),
    ABSORBER("absorber", () -> new Absorber(CURIOS_RARE)),
    DEIFIC_AMULET("deific_amulet", () -> new DeificAmulet(CURIOS_UNCOMMON)),
    BLAZING_CORE("blazing_core", () -> new BlazingCore(CURIOS_UNCOMMON)),
    BLOODFLARE_CORE("bloodflare_core", () -> new BloodflareCore(CURIOS_UNCOMMON)),
    EVOLUTION("evolution", () -> new Evolution(CURIOS_EPIC)),
    NEBULOUS_CORE("nebulous_core", () -> new NebulousCore(CURIOS_RARE)),
    SPONGE("sponge", () -> new Sponge(CURIOS_RARE)),
    BLOOD_GOD_CHALICE("blood_god_chalice", () -> new BloodGodChalice(CURIOS_EPIC)),
    DEITIES_RAMPART("deities_rampart", () -> new DeitiesRampart(CURIOS_EPIC)),
    DRAEDON_HEART("draedon_heart", () -> new DraedonHeart(CURIOS_EPIC)),
    HARPY_RING("harpy_ring", () -> new HarpyRing(CURIOS_COMMON)),
    AERO_STONE("aero_stone", () -> new AeroStone(CURIOS_COMMON)),
    DEEP_DIVER("deep_diver", () -> new DeepDiver(CURIOS_RARE)),
    ORNATE_SHIELD("ornate_shield", () -> new OrnateShield(CURIOS_UNCOMMON)),
    ANGEL_TREADS("angel_treads", () -> new AngelTreads(CURIOS_UNCOMMON)),
    ASGARD_VALOR("asgard_valor", () -> new AsgardValor(CURIOS_RARE)),
    LEVIATHAN_AMBERGRIS("leviathan_ambergris", () -> new LeviathanAmbergris(CURIOS_COMMON)),
    GRAVISTAR_SABATON("gravistar_sabaton", () -> new GravistarSabaton(CURIOS_COMMON)),
    GRUESOME_EMINENCE("gruesome_eminence", () -> new GruesomeEminence(CURIOS_CALAMITY)),
    SHIELD_OF_THE_HIGH_RULER("shield_of_the_high_ruler", () -> new ShieldOfTheHighRuler(CURIOS_UNCOMMON)),
    STATIS_NINJA_BELT("statis_ninja_belt", () -> new StatisNinjaBelt(CURIOS_RARE, false)),
    STATIS_VOID_SASH("statis_void_sash", () -> new StatisNinjaBelt(CURIOS_EPIC, true)),
    ELYSIAN_AEGIS("elysian_aegis", () -> new ElysianAegis(CURIOS_UNCOMMON)),
    ASCENDANT_INSIGNIA("ascendant_insignia", () -> new AscendantInsignia(CURIOS_UNCOMMON)),
    OLD_DUKE_SCALES("old_duke_scales", () -> new OldDukeScales(CURIOS_UNCOMMON)),
    CRIMSON_FLASK("crimson_flask", () -> new Flask(CURIOS_COMMON, Level.NETHER, "crimson_flask", MobEffects.MOVEMENT_SLOWDOWN)),
    CORRUPT_FLASK("corrupt_flask", () -> new Flask(CURIOS_COMMON, Level.OVERWORLD, "corrupt_flask", MobEffects.BLINDNESS)),
    ASGARDIAN_AEGIS("asgardian_aegis", () -> new AsgardianAegis(CURIOS_EPIC)),
    GLADIATOR_LOCKET("gladiator_locket", () -> new GladiatorLocket(CURIOS_UNCOMMON)),
    FROST_FLARE("frost_flare", () -> new FrostFlare(CURIOS_RARE)),
    COMMUNITY("community", () -> new Community(CURIOS_EPIC)),
    CAMPER("camper", () -> new Camper(CURIOS_UNCOMMON)),
    DARK_SUN_RING("dark_sun_ring", () -> new DarkSunRing(CURIOS_EPIC)),
    AMALGAM("amalgam", () -> new Amalgam(CURIOS_EPIC)),
    COIN_OF_DECEIT("coin_of_deceit", () -> new CoinOfDeceit(CURIOS_COMMON)),
    SCUTTLERS_JEWEL("scuttlers_jewel", () -> new ScuttlersJewel(CURIOS_COMMON)),
    RAIDERS_TALISMAN("raiders_talisman", () -> new RaidersTalisman(CURIOS_COMMON)),
    ROTTEN_DOG_TOOTH("rotten_dog_tooth", () -> new RottenDogTooth(CURIOS_COMMON)),
    INK_BOMB("ink_bomb", () -> new InkBomb(CURIOS_UNCOMMON)),
    SILENCING_SHEATH("silencing_sheath", () -> new SilencingSheath(CURIOS_COMMON)),
    BLOODSTAINED_GLOVE("bloodstained_glove", () -> new BloodstainedGlove(CURIOS_COMMON)),
    FILTHY_GLOVE("filthy_glove", () -> new FilthyGlove(CURIOS_COMMON)),
    FEATHER_CROWN("feather_crown", () -> new FeatherCrown(CURIOS_COMMON)),
    MIRAGE_MIRROR("mirage_mirror", () -> new MirageMirror(CURIOS_COMMON)),
    CORROSIVE_SPINE("corrosive_spine", () -> new CorrosiveSpine(CURIOS_COMMON)),
    ELECTRICIANS_GLOVE("electricians_glove", () -> new ElectriciansGlove(CURIOS_UNCOMMON)),
    RUIN_MEDALLION("ruin_medallion", () -> new RuinMedallion(CURIOS_UNCOMMON)),
    VAMPIRIC_TALISMAN("vampiric_talisman", () -> new VampiricTalisman(CURIOS_UNCOMMON)),
    PRECISION_GLOVE("precision_glove", () -> new PrecisionGlove(CURIOS_COMMON)),
    RECKLESSNESS_GLOVE("recklessness_glove", () -> new RecklessnessGlove(CURIOS_COMMON)),
    ABYSSAL_MIRROR("abyssal_mirror", () -> new AbyssalMirror(CURIOS_RARE)),
    ETHEREAL_EXTORTER("ethereal_extorter", () -> new EtherealExtorter(CURIOS_UNCOMMON)),
    PLAGUE_FUEL_PACK("plagued_fuel_pack", () -> new PlagueFuelPack(CURIOS_RARE)),
    DARK_MATTER_SHEATH("dark_matter_sheath", () -> new DarkMatterSheath(CURIOS_RARE)),
    MOONSTONE_CROWN("moonstone_crown", () -> new MoonstoneCrown(CURIOS_RARE)),
    BLUNDER_BOOSTER("blunder_booster", () -> new BlunderBooster(CURIOS_EPIC)),
    SPECTRAL_VEIL("spectral_veil", () -> new SpectralVeil(CURIOS_RARE)),
    VENERATED_LOCKET("venerated_locket", () -> new VeneratedLocket(CURIOS_EPIC)),
    ORACLE_DECK("oracle_deck", OracleDeck::new),
    FATE_THREAD("fate_thread", () -> new UnsealingRope("fate_thread")),
    ABYSS_THREAD("abyss_thread", () -> new UnsealingRope("abyss_thread")),
    BRILLIANCE("brilliance", () -> new Brilliance(CURIOS_UNCOMMON)),
    AURA("aura", () -> new Aura(CURIOS_UNCOMMON)),
    INSPIRATION("inspiration", () -> new Inspiration(CURIOS_UNCOMMON)),
    ENDURANCE("endurance", () -> new Endurance(CURIOS_UNCOMMON)),
    ENTITY("entity", () -> new Entity(CURIOS_UNCOMMON)),
    WISDOM("wisdom", () -> new Wisdom(CURIOS_UNCOMMON)),
    METROPOLIS("metropolis", () -> new Metropolis(CURIOS_UNCOMMON)),
    RADIANCE_CARD("radiance_card", () -> new RadianceCard(CURIOS_UNCOMMON)),
    TEMPERANCE("temperance", () -> new Temperance(CURIOS_UNCOMMON)),
    WYRM_TOOTH_NECKLACE("wyrm_tooth_necklace", () -> new WyrmToothNecklace(CURIOS_CALAMITY)),
    DEUS_CORE("deus_core", () -> new DeusCore(CURIOS_EPIC)),
    FORESEE_ORB("foresee_orb", () -> new ForeseeOrb(CURIOS_RARE)),
    SACRIFICES_MASK("sacrifices_mask", () -> new SacrificesMask(CURIOS_UNCOMMON)),
    TAINTED_DECK("tainted_deck", TaintedDeck::new),
    CONFUSE("confuse", () -> new Confuse(CURIOS_EPIC)),
    BARREN("barren", () -> new Barren(CURIOS_EPIC)),
    FRAIL("frail", () -> new Frail(CURIOS_EPIC)),
    NOTHING("nothing", () -> new Nothing(CURIOS_EPIC)),
    GREED("greed", () -> new Greed(CURIOS_EPIC)),
    FOOL("fool", () -> new Fool(CURIOS_EPIC)),
    TARNISH("tarnish", () -> new Tarnish(CURIOS_EPIC)),
    PERPLEXED("perplexed", () -> new Perplexed(CURIOS_EPIC)),
    SACRIFICE("sacrifice", () -> new Sacrifice(CURIOS_EPIC)),
    HOLY_MOONLIGHT("holy_moonlight", () -> new HolyMoonlight(CURIOS_EPIC)),
    STAR_CHARM("star_charm", () -> new StarCharm(CURIOS_UNCOMMON)),

    //Item
    COMET_SHARD("comet_shard", () -> new MagicItem(RARE_ONE, 0, "comet_shard")),

    ETHEREAL_CORE("ethereal_core", () -> new MagicItem(UNCOMMON_ONE, 1, "ethereal_core")),

    PHANTOM_HEART("phantom_heart", () -> new MagicItem(EPIC_ONE, 2, "phantom_heart")),

    ENCHANTED_STARFISH("enchanted_starfish", () -> new EnchantedStarfish(
        getFoodProperties(Rarity.COMMON, 16, null))),

    WEAK_MANA_POTION("weak_mana_potion", () -> new ManaPotion(
        getFoodProperties(Rarity.COMMON, 16, null), 10, 15)),

    MANA_POTION("mana_potion", () -> new ManaPotion(
        getFoodProperties(Rarity.COMMON, 16, null), 25, 20)),

    STRONG_MANA_POTION("strong_mana_potion", () -> new ManaPotion(
        getFoodProperties(Rarity.UNCOMMON, 16, null), 50, 25)),

    SUPER_MANA_POTION("super_mana_potion", () -> new ManaPotion(
        getFoodProperties(Rarity.RARE, 16, null), 100, 30)),

    SUPREME_MANA_POTION("supreme_mana_potion", () -> new ManaPotion(
        getFoodProperties(Rarity.EPIC, 16, null), 200, 35, false)),

    MUSHROOM_PLASMA_ROOT("mushroom_plasma_root", () -> new RageItem(RARE_ONE, 0, 20)),

    INFERNAL_BLOOD("infernal_blood", () -> new RageItem(UNCOMMON_ONE, 1, 20)),

    LIGHTNING_CONTAINER("red_lightning_container", () -> new RageItem(EPIC_ONE, 2, 20)),

    NIGHT_SOUL("night_soul", () -> new TooltipItem(ITEM_COMMON, "night_soul", 1)),

    LIGHT_SOUL("light_soul", () -> new TooltipItem(ITEM_COMMON, "light_soul", 1)),

    SILK("silk", () -> new Item(ITEM_COMMON)),

    UELIBLOOM("uelibloom_ingot", () -> new Item(ITEM_UNCOMMON)),

    ASCENDANT_SPIRIT_ESSENCE("ascendant_spirit_essence", () -> new TooltipItem(ITEM_EPIC, "essence", 1)),

    POLTERPLASM("polterplasm", () -> new TooltipItem(ITEM_RARE, "polterplasm", 1)),

    NIGHTMARE_FUEL("nightmare_fuel", () -> new TooltipItem(ITEM_UNCOMMON, "nightmare_fuel", 1)),

    ENDOTHERMIC_ENERGY("endothermic_energy", () -> new TooltipItem(ITEM_RARE, "energy", 1)),

    DARK_SUN_FRAGMENT("dark_sun_fragment", () -> new TooltipItem(ITEM_UNCOMMON, "fragment", 1)),

    GALACTICA_SINGULARITY("galactica_singularity", () -> new NoGravityItem(ITEM_EPIC, "singularity", 1)),

    VORTEX("vortex_fragment", () -> new NoGravityItem(ITEM_RARE, "vortex", 1)),

    NEBULA("nebula_fragment", () -> new NoGravityItem(ITEM_UNCOMMON, "nebula", 1)),

    STARDUST("stardust_fragment", () -> new NoGravityItem(ITEM_RARE, "stardust", 1)),

    SOLAR("solar_fragment", () -> new NoGravityItem(ITEM_UNCOMMON, "solar", 1)),

    MELD_CONSTRUCT("meld_construct", () -> new Item(ITEM_EPIC)),

    MELD_BLOB("meld_blob", () -> new Item(ITEM_UNCOMMON)),

    ELECTROLYTE_GEL_PACK("electrolyte_gel_pack", () -> new AdrenalineItem(RARE_ONE, 0)),

    STARLIGHT_FUEL_CELL("starlight_fuel_cell", () -> new AdrenalineItem(UNCOMMON_ONE, 1)),

    ECTOHEART("ectoheart", () -> new AdrenalineItem(EPIC_ONE, 2)),

    CELESTIAL_ONION("celestial_onion", () -> new CelestialOnion(EPIC_ONE)),

    BLOOD_ORANGE("blood_orange", () -> new LifeFruit(getFoodProperties(Rarity.COMMON, 1, new FoodProperties.Builder()
        .alwaysEat().saturationMod(6f).nutrition(10).build()), "blood_orange")),

    MIRACLE_FRUIT("miracle_fruit", () -> new LifeFruit(getFoodProperties(Rarity.UNCOMMON, 1, new FoodProperties.Builder()
        .alwaysEat().saturationMod(10f).nutrition(14).build()), "miracle_fruit")),

    ELDER_BERRY("elder_berry", () -> new LifeFruit(getFoodProperties(Rarity.RARE, 1, new FoodProperties.Builder()
        .alwaysEat().saturationMod(14f).nutrition(18).build()), "elder_berry")),

    DRAGON_FRUIT("dragon_fruit", () -> new LifeFruit(getFoodProperties(Rarity.EPIC, 1, new FoodProperties.Builder()
        .alwaysEat().saturationMod(20f).nutrition(20).build()), "dragon_fruit")),

    HADAL_STEW("hadal_stew", () -> new HadalStew(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    AUREUS_CELL("aureus_cell", () -> new AureusCell(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    GRAPE_BEER("grape_beer", () -> new GrapeBeer(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    MARGARITA("margarita", () -> new Margarita(getFoodProperties(Rarity.RARE, 16, null))),

    RED_WINE("red_wine", () -> new RedWine(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    WHITE_WINE("white_wine", () -> new WhiteWine(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    BAGUETTE("baguette", () -> new Baguette(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    DELICIOUS_MEAT("delicious_meat", () -> new DeliciousMeat(getFoodProperties(Rarity.RARE, 16, null))),

    ODD_MUSHROOM("odd_mushroom", () -> new OddMushroom(getFoodProperties(Rarity.EPIC, 16, null))),

    BLASPHEMOUS_DONUT("blasphemous_donut", () -> new BlasphemousDonut(getFoodProperties(Rarity.UNCOMMON, 16, null))),

    LAVA_CHICKEN_BROTH("lava_chicken_broth", () -> new LavaChickenBroth(getFoodProperties(Rarity.EPIC, 16, null))),

    ANECHOIC_COATING("anechoic_coating", () -> new CalamityPotion(ITEM_RARE, "anechoic_coating", () ->
        new MobEffectInstance(CalamityEffects.ANECHOIC_COATING.get(), 1200)).setTextColor(5636095)),

    BOUNDING("bounding", () -> new CalamityPotion(getPotionProperties(Rarity.RARE), "bounding", () ->
        new MobEffectInstance(CalamityEffects.BOUNDING.get(), 600)).setTextColor(3255451)),

    CALCIUM("calcium", () -> new CalamityPotion(getPotionProperties(Rarity.COMMON), "calcium", () ->
        new MobEffectInstance(CalamityEffects.CALCIUM.get(), 600)).setTextColor(9801814)),

    TESLA("tesla", () -> new CalamityPotion(getPotionProperties(Rarity.RARE), "tesla", () ->
        new MobEffectInstance(CalamityEffects.TESLA.get(), 900)).setTextColor(5636095)),

    ZEN("zen", () -> new CalamityPotion(getPotionProperties(Rarity.RARE), "zen", () ->
        new MobEffectInstance(CalamityEffects.ZEN.get(), 6000)).setTextColor(16777215)),

    ZERG("zerg", () -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "zerg", () ->
        new MobEffectInstance(CalamityEffects.ZERG.get(), 6000)).setTextColor(11141290)),

    PHOTOSYNTHESIS("photosynthesis", () -> new CalamityPotion(getPotionProperties(Rarity.UNCOMMON), "photosynthesis", () ->
        new MobEffectInstance(CalamityEffects.PHOTOSYNTHESIS.get(), 600)).setTextColor(16777045)),

    ASTRAL_INJECTION("astral_injection", () -> new CalamityPotion(getPotionProperties(Rarity.RARE), "astral_injection", () ->
        new MobEffectInstance(CalamityEffects.ASTRAL_INJECTION.get(), 140)).setTextColor(5636095)),

    CEASELESS_HUNGER("ceaseless_hunger", () -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "ceaseless_hunger", () ->
        new MobEffectInstance(CalamityEffects.CEASELESS_HUNGER.get(), 200)).setTextColor(16733695)),

    OMNISCIENCE("omniscience", () -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "omniscience", () ->
        new MobEffectInstance(CalamityEffects.OMNISCIENCE.get(), 1200)).setTextColor(16733695)),

    CINNAMON_SOLYN("cinnamon_solyn", () -> new CinnamonSolyn(getFoodProperties(Rarity.EPIC, 16, null))),

    GOOD_APPLE("good_apple", () -> new GoodApple(getFoodProperties(Rarity.EPIC, 1, null))),

    BRIMSTONE_LOCUS("brimstone_locus", () -> new BrimstoneLocus(ITEM_CALAMITY, "brimstone_locus", 1)),

    PERVERSE_PURSE("perverse_purse", () -> new PerversePurse(UNCOMMON_ONE)),

    //SwordAndTool
    ETERNITY("eternity", () -> {
        setUniqueProperties(EPIC_ONE);
        return new Eternity();
    }),

    NEBULOUS_CATACLYSM("nebulous_cataclysm", () -> new NebulousCataclysm(EPIC_ONE)),

    RANCOR("rancor", () -> new Rancor(ITEM_CALAMITY)),

    EXCELSUS("excelsus", () -> new Excelsus(RegisterList.EXCELSUS, 14, -2f, EPIC_ONE)),

    ATARAXIA("ataraxia", () -> new Ataraxia(RegisterList.EXCELSUS, 11, -2f, EPIC_ONE)),

    UNIVERSE_SPLITTER("universe_splitter", () -> new UniverseSplitter(EPIC_ONE)),

    YHARIMS_CRYSTAL("yharims_crystal", () -> new YharimsCrystal(ITEM_CALAMITY)),

    CRYSTYL_CRUSHER("crystyl_crusher", () -> new CrystylCrusher(EPIC_ONE)),

    ZENITH("zenith", () -> new Zenith(Tiers.NETHERITE, 10, -2f,
        new Item.Properties().defaultDurability(-1).rarity(Rarity.EPIC).tab(CALAMITY_ITEM)));

    private final RegistryObject<Item> value;

    CalamityItems(String id, Supplier<Item> curio) {
        this.value = ITEMS.register(id, curio);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public ResourceLocation getId() {
        return value.getId();
    }

    @Override
    public RegistryObject<Item> getValue() {
        return value;
    }

    public boolean isEquip(LivingEntity player) {
        return CalamityHelp.hasCurio(player, this.get());
    }
}
