package hua223.calamity.register.items;

import hua223.calamity.integration.curios.item.*;
import hua223.calamity.integration.curios.item.entropy.*;
import hua223.calamity.register.items.edible.*;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static hua223.calamity.register.RegisterList.*;

public enum CalamityItems implements EnumRegister<Item>/*, IExtensibleEnum */ {
    //Curios
    YHARIM_GIFT("yharim_gift", () -> new YharimGift(CURIOS_UNCOMMON)),
    RADIANCE("radiance", () -> new Radiance(CURIOS_EPIC)),
    BLOODY_WORM_TOOTH("bloody_worm_tooth", () -> new BloodyWormTooth(CURIOS_COMMON)), //血蠕虫之牙
    BLOODY_WORM_SCARF("bloody_worm_scarf", () -> new BloodyWormScarf(CURIOS_UNCOMMON)),
    COUNTER_SCARF("counter_scarf", () -> new CounterScarf(CURIOS_COMMON)),
    EVASION_SCARF("evasion_scarf", () -> new EvasionScarf(CURIOS_UNCOMMON)),
    HIDE_OF_ASTRUM_DEUS("hide_of_astrum_deus", () -> new AstrumDeusHide(CURIOS_UNCOMMON)),
    WARBANNER_SUN("warbanner_of_the_sun", () -> new SunWarBanner(CURIOS_UNCOMMON)),
    BADGE_BRAVERY("badge_of_bravery", () -> new BraveryBadge(CURIOS_COMMON)),
    ELEMENTAL_GAUNTLET("elemental_gauntlet", () -> new ElementalGauntlet(CURIOS_EPIC)),
    NIHILITY_QUIVER("nihility_quiver", () -> new NihilityQuiver(CURIOS_EPIC)),
    SCIONS_CURIO("scions_curio", () -> new ScionsCurio(CURIOS_COMMON)),
    DEADSHOT_BROOCH("deadshot_brooch", () -> new DeadshotBrooch(CURIOS_UNCOMMON)),
    DAAWNLIGHT("daawnlight_spirit_origin", () -> new DaawnlightSpiritOrigin(CURIOS_RARE)),
    STEM_CELLS("dynamo_stem_cells", () -> new DynamoStemCells(CURIOS_UNCOMMON)),
    PLANEBREAKERS_POUCH("planebreakers_pouch", () -> new PlanebreakersPouch(CURIOS_EPIC)),
    CONCOCTION("permafrost_concoction", () -> new PermafrostConcoction(CURIOS_RARE)),
    MANA_POLARIZER("mana_polarizer", () -> new ManaPolarizer(CURIOS_COMMON)),
    CHAOS_STONE("chaos_stone", () -> new ChaosStone(CURIOS_UNCOMMON)),
    CALAMITY_SIGIL("calamity_sigil", () -> new CalamitySigil(CURIOS_UNCOMMON)),
    ETHEREAL_TALISMAN("ethereal_talisman", () -> new EtherealTalisman(CURIOS_RARE)),
    CRAW_CARAPACE("craw_carapace", () -> new CrawCarapace(CURIOS_COMMON)),
    ABYSSAL_AMULET("abyssal_amulet", () -> new AbyssalAmulet(CURIOS_UNCOMMON)),
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
    BAROCLAW("baroclaw", () -> new Baroclaw(CURIOS_UNCOMMON)),
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
    MOON_WALKERS("moon_walkers", () -> new MoonWalkers(CURIOS_RARE)),
    VOID_STRIDERS("void_striders", () -> new VoidStriders(CURIOS_EPIC)),
    SERAPH_TRACERS("seraph_tracers", () -> new SeraphTracers(ITEM_CALAMITY)),
    ASGARD_VALOR("asgard_valor", () -> new AsgardValor(CURIOS_RARE)),
    LEVIATHAN_AMBERGRIS("leviathan_ambergris", () -> new LeviathanAmbergris(CURIOS_RARE)),
    GRAVISTAR_SABATON("gravistar_sabaton", () -> new GravistarSabaton(CURIOS_UNCOMMON)),
    GRUESOME_EMINENCE("gruesome_eminence", () -> new GruesomeEminence(CURIOS_CALAMITY)),
    SHIELD_OF_THE_HIGH_RULER("shield_of_the_high_ruler", () -> new ShieldOfTheHighRuler(CURIOS_UNCOMMON)),
    STATIS_NINJA_BELT("statis_ninja_belt", () -> new StatisNinjaBelt(CURIOS_RARE, false)),
    STATIS_VOID_SASH("statis_void_sash", () -> new StatisNinjaBelt(CURIOS_EPIC, true)),
    ELYSIAN_AEGIS("elysian_aegis", () -> new ElysianAegis(CURIOS_UNCOMMON)),
    ASCENDANT_INSIGNIA("ascendant_insignia", () -> new AscendantInsignia(CURIOS_UNCOMMON)),
    OLD_DUKE_SCALES("old_duke_scales", () -> new OldDukeScales(CURIOS_UNCOMMON)),
    CRIMSON_FLASK("crimson_flask", () -> new Flask(CURIOS_UNCOMMON, Level.NETHER, "crimson_flask", MobEffects.MOVEMENT_SLOWDOWN)),
    CORRUPT_FLASK("corrupt_flask", () -> new Flask(CURIOS_UNCOMMON, Level.OVERWORLD, "corrupt_flask", MobEffects.BLINDNESS)),
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
    FUNGAL_CLUMP("fungal_clump", () -> new FungalClump(CURIOS_UNCOMMON)),
    ODINS_REFUGE("odins_refuge", () -> new OdinsRefuge(CURIOS_EPIC)),
    SILVAS_CROWN("silvas_crown", () -> new SilvasCrown(CURIOS_UNCOMMON)),
    NIHILITY_SHELL("nihility_shell", () -> new NihilityShell(CURIOS_EPIC)),
    AZURE_ABYSS_TALISMAN("azure_abyss_talisman", () -> new AzureAbyssTalisman(CURIOS_RARE)),
    ARC_FLASH_RING("arc_flash_ring", () -> new ArcFlashRing(CURIOS_RARE)),
    ECLIPSE_MIRROR("eclipse_mirror", EclipseMirror::new),

    //Item
    COMET_SHARD(() -> new MagicItem(RARE_ONE, 0), "comet_shard"),

    ETHEREAL_CORE(() -> new MagicItem(UNCOMMON_ONE, 1), "ethereal_core"),

    PHANTOM_HEART(() -> new MagicItem(EPIC_ONE, 2), "phantom_heart"),

    ENCHANTED_STARFISH(() -> new EnchantedStarfish(
        getFoodProperties(Rarity.COMMON, 16, null)), "enchanted_starfish"),

    WEAK_MANA_POTION(() -> new ManaPotion(
        getFoodProperties(Rarity.COMMON, 16, null), 10, 15), "weak_mana_potion"),

    MANA_POTION(() -> new ManaPotion(
        getFoodProperties(Rarity.COMMON, 16, null), 25, 20), "mana_potion"),

    STRONG_MANA_POTION(() -> new ManaPotion(
        getFoodProperties(Rarity.UNCOMMON, 16, null), 50, 25), "strong_mana_potion"),

    SUPER_MANA_POTION(() -> new ManaPotion(
        getFoodProperties(Rarity.RARE, 16, null), 100, 30), "super_mana_potion"),

    SUPREME_MANA_POTION(() -> new ManaPotion(
        getFoodProperties(Rarity.EPIC, 16, null), 200, 35, false), "supreme_mana_potion"),

    MUSHROOM_PLASMA_ROOT(() -> new RageItem(RARE_ONE, 0), "mushroom_plasma_root"),

    INFERNAL_BLOOD(() -> new RageItem(UNCOMMON_ONE, 1), "infernal_blood"),

    LIGHTNING_CONTAINER(() -> new RageItem(EPIC_ONE, 2), "red_lightning_container"),

    ASCENDANT_SPIRIT_ESSENCE(() -> new TooltipItem(ITEM_EPIC, "essence"), "ascendant_spirit_essence"),

    NECROPLASM(Necroplasm::new, "necroplasm"),

    NIGHTMARE_FUEL(NightmareFuel::new, "nightmare_fuel"),

    ENDOTHERMIC_ENERGY(EndothermicEnergy::new, "endothermic_energy"),

    DARK_SUN_FRAGMENT(() -> new TooltipItem(ITEM_UNCOMMON, "dark_sun_fragment"), "dark_sun_fragment"),

    GALACTICA_SINGULARITY(() -> new NoGravityItem(ITEM_EPIC, "singularity"), "galactica_singularity"),

    VORTEX(() -> new NoGravityItem(ITEM_RARE, "vortex"), "vortex_fragment"),

    NEBULA(() -> new NoGravityItem(ITEM_UNCOMMON, "nebula"), "nebula_fragment"),

    STARDUST(() -> new NoGravityItem(ITEM_RARE, "stardust"), "stardust_fragment"),

    SOLAR(() -> new NoGravityItem(ITEM_UNCOMMON, "solar"), "solar_fragment"),

    MELD_CONSTRUCT(() -> new Item(ITEM_EPIC), "meld_construct"),

    MELD_BLOB(() -> new Item(ITEM_UNCOMMON), "meld_blob"),

    ELECTROLYTE_GEL_PACK(() -> new AdrenalineItem(RARE_ONE, 0), "electrolyte_gel_pack"),

    STARLIGHT_FUEL_CELL(() -> new AdrenalineItem(UNCOMMON_ONE, 1), "starlight_fuel_cell"),

    ECTOHEART(() -> new AdrenalineItem(EPIC_ONE, 2), "ectoheart"),

    CELESTIAL_ONION(() -> new CelestialOnion(EPIC_ONE), "celestial_onion"),

    SANGUINE_TANGERINE(() -> new LifeFruit(Rarity.UNCOMMON, 1), "sanguine_tangerine"),

    MIRACLE_FRUIT(() -> new LifeFruit(Rarity.RARE, 2), "miracle_fruit"),

    TAINTED_CLOUDBERRY(() -> new LifeFruit(Rarity.EPIC, 3), "tainted_cloudberry"),

    SACRED_STRAWBERRY(() -> new LifeFruit(RegisterList.CALAMITY, 4), "sacred_strawberry"),

    HADAL_STEW(() -> new HadalStew(getFoodProperties(Rarity.UNCOMMON, 16, null)), "hadal_stew"),

    AUREUS_CELL(() -> new AureusCell(getFoodProperties(Rarity.UNCOMMON, 16, null)), "aureus_cell"),

    GRAPE_BEER(() -> new GrapeBeer(getFoodProperties(Rarity.UNCOMMON, 16, null)), "grape_beer"),

    MARGARITA(() -> new Margarita(getFoodProperties(Rarity.RARE, 16, null)), "margarita"),

    RED_WINE(() -> new RedWine(getFoodProperties(Rarity.UNCOMMON, 16, null)), "red_wine"),

    WHITE_WINE(() -> new WhiteWine(getFoodProperties(Rarity.UNCOMMON, 16, null)), "white_wine"),

    BAGUETTE(() -> new Baguette(getFoodProperties(Rarity.UNCOMMON, 16, null)), "baguette"),

    DELICIOUS_MEAT(() -> new DeliciousMeat(getFoodProperties(Rarity.RARE, 16, null)), "delicious_meat"),

    ODD_MUSHROOM(() -> new OddMushroom(getFoodProperties(Rarity.EPIC, 16, null)), "odd_mushroom"),

    BLASPHEMOUS_DONUT(() -> new BlasphemousDonut(getFoodProperties(Rarity.UNCOMMON, 16, null)), "blasphemous_donut"),

    LAVA_CHICKEN_BROTH(() -> new LavaChickenBroth(getFoodProperties(Rarity.EPIC, 16, null)), "lava_chicken_broth"),

    ANECHOIC_COATING(() -> new CalamityPotion(ITEM_RARE, "anechoic_coating", () ->
        new MobEffectInstance(CalamityEffects.ANECHOIC_COATING.get(), 1200)).setTextColor(5636095), "anechoic_coating"),

    BOUNDING(() -> new CalamityPotion(getPotionProperties(Rarity.RARE), "bounding", () ->
        new MobEffectInstance(CalamityEffects.BOUNDING.get(), 600)).setTextColor(3255451), "bounding"),

    CALCIUM(() -> new CalamityPotion(getPotionProperties(Rarity.COMMON), "calcium", () ->
        new MobEffectInstance(CalamityEffects.CALCIUM.get(), 600)).setTextColor(9801814), "calcium"),

    TESLA(() -> new CalamityPotion(getPotionProperties(Rarity.RARE), "tesla", () ->
        new MobEffectInstance(CalamityEffects.TESLA.get(), 900)).setTextColor(5636095), "tesla"),

    ZEN(() -> new CalamityPotion(getPotionProperties(Rarity.RARE), "zen", () ->
        new MobEffectInstance(CalamityEffects.ZEN.get(), 6000)).setTextColor(16777215), "zen"),

    ZERG(() -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "zerg", () ->
        new MobEffectInstance(CalamityEffects.ZERG.get(), 6000)).setTextColor(11141290), "zerg"),

    PHOTOSYNTHESIS(() -> new CalamityPotion(getPotionProperties(Rarity.UNCOMMON), "photosynthesis", () ->
        new MobEffectInstance(CalamityEffects.PHOTOSYNTHESIS.get(), 600)).setTextColor(16777045), "photosynthesis"),

    ASTRAL_INJECTION(() -> new CalamityPotion(getPotionProperties(Rarity.RARE), "astral_injection", () ->
        new MobEffectInstance(CalamityEffects.ASTRAL_INJECTION.get(), 140)).setTextColor(5636095), "astral_injection"),

    CEASELESS_HUNGER(() -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "ceaseless_hunger", () ->
        new MobEffectInstance(CalamityEffects.CEASELESS_HUNGER.get(), 200)).setTextColor(16733695), "ceaseless_hunger"),

    OMNISCIENCE(() -> new CalamityPotion(getPotionProperties(Rarity.EPIC), "omniscience", () ->
        new MobEffectInstance(CalamityEffects.OMNISCIENCE.get(), 1200)).setTextColor(16733695), "omniscience"),

    CINNAMON_SOLYN(() -> new CinnamonSolyn(getFoodProperties(Rarity.EPIC, 16, null)), "cinnamon_solyn"),

    GOOD_APPLE(() -> new GoodApple(getFoodProperties(Rarity.EPIC, 1, null)), "good_apple"),

    BRIMSTONE_LOCUS(() -> new BrimstoneLocus(ITEM_CALAMITY, "brimstone_locus", 1), "brimstone_locus"),

    PERVERSE_PURSE(() -> new PerversePurse(UNCOMMON_ONE), "perverse_purse"),

    DEMON_SHADE_HELMET(() -> new DemonShade(ArmorItem.Type.HELMET), "demon_shade_helmet"),

    DEMON_SHADE_CHEST(() -> new DemonShade(ArmorItem.Type.CHESTPLATE), "demon_shade_chest"),

    DEMON_SHADE_LEGGINGS(() -> new DemonShade(ArmorItem.Type.LEGGINGS), "demon_shade_leggings"),

    DEMON_SHADE_BOOTS(() -> new DemonShade(ArmorItem.Type.BOOTS), "demon_shade_boots"),

    SHADOW_SPEC_BAR(() -> new TooltipItem(new Item.Properties().stacksTo(64).fireResistant()
            .rarity(RegisterList.CALAMITY), "shadow_spec_bar"), "shadow_spec_bar"),

    SUNLIGHT_ESSENCE(() -> new SunlightEssence(ITEM_COMMON), "sunlight_essence"),

    PURIFIED_GEL(() -> new Item(ITEM_COMMON), "purified_gel"),

    BLIGHTED_GEL(() -> new Item(ITEM_COMMON), "blighted_gel"),

    AURIC_INGOT(() -> new TooltipItem(ITEM_UNCOMMON, "auric_ingot"), "auric_ingot"),

    YHARON_SOUL_FRAGMENT(() -> new TooltipItem(ITEM_UNCOMMON, "yharon_soul_fragment"), "yharon_soul_fragment"),

    NECROPLASMIC_BEACON(NecroplasmicBeacon::new, "necroplasmic_beacon"),

    BLOOD_STONE(() -> new Item(ITEM_COMMON), "blood_stone"),

    BLOODSTONE_CORE(() -> new Item(ITEM_COMMON), "bloodstone_core"),

    LUMENYL(() -> new Item(ITEM_COMMON), "lumenyl"),

    TERMINUS(Terminus::new, "terminus"),

    //SwordAndTool
    ETERNITY(() -> {
        setUniqueProperties(EPIC_ONE);
        return new Eternity();
    }, "eternity"),

    NEBULOUS_CATACLYSM(() -> new NebulousCataclysm(EPIC_ONE), "nebulous_cataclysm"),

    RANCOR(() -> new Rancor(ITEM_CALAMITY), "rancor"),

    EXCELSUS(() -> new Excelsus(14, -2f, EPIC_ONE), "excelsus"),

    ATARAXIA(() -> new Ataraxia(11, -2f, EPIC_ONE), "ataraxia"),

    UNIVERSE_SPLITTER(() -> new UniverseSplitter(EPIC_ONE), "universe_splitter"),

    YHARIMS_CRYSTAL(() -> new YharimsCrystal(ITEM_CALAMITY), "yharims_crystal"),

    CRYSTYL_CRUSHER(() -> new CrystylCrusher(EPIC_ONE), "crystyl_crusher"),

    DREAM_CATCHER(() -> new DreamCatcher(EPIC_ONE), "dream_catcher"),

    ANTI_VOID(() -> new AntiVoid(EPIC_ONE), "anti_void"),

    STARLESS_NIGHT(() -> new StarlessNight(RARE_ONE), "starless_night"),

    XYTHERON(Xytheron::new, "xytheron"),

    STORM_MAIDENS_RETRIBUTION(() -> new StormMaidensRetribution(ITEM_CALAMITY), "storm_maidens_retribution"),

    ICE_SNOW_MIRROR(IceSnowMirror::new, "ice_mirror"),

    BRIMSTONE_BARRIER(BrimstoneCrescentStaff::new, "brimstone_crescent_staff"),

    SEAFOAM_BOMB(SeafoamBomb::new, "seafoam_bomb"),

    BALLISTIC_POISON_BOMB(BallisticPoisonBomb::new, "ballistic_poison_bomb"),

    ZENITH(() -> new Zenith(10, -2f,
        new Item.Properties().defaultDurability(-1).rarity(Rarity.EPIC)), "zenith");

    private final RegistryObject<Item> value;

    CalamityItems(String id, Supplier<Item> curio) {
        this.value = ITEMS.register(id, curio);
        curiosIndex++;
    }

    CalamityItems(Supplier<Item> curio, String id) {
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
        return CalamityHelp.hasCurio(player, get());
    }
}