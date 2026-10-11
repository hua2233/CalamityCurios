package hua223.calamity.register.items;

import hua223.calamity.integration.curios.item.*;
import hua223.calamity.integration.curios.item.entropy.*;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.items.edible.*;
import hua223.calamity.register.rune.blessing.*;
import hua223.calamity.register.rune.blessing.Void;
import hua223.calamity.register.rune.fragments.Body;
import hua223.calamity.register.rune.fragments.Echo;
import hua223.calamity.register.rune.fragments.Mind;
import hua223.calamity.register.rune.fragments.Soul;
import hua223.calamity.register.rune.gem.*;
import hua223.calamity.register.rune.scroll.*;
import hua223.calamity.register.rune.stone.*;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static hua223.calamity.register.RegisterList.*;

public enum CalamityItems implements EnumRegister<Item>/*, IExtensibleEnum */ {
    //Curios
    YHARIM_GIFT("yharim_gift", () -> new YharimGift(UNCOMMON_ONE)),
    RADIANCE("radiance", () -> new Radiance(EPIC_ONE)),
    BLOODY_WORM_TOOTH("bloody_worm_tooth", () -> new BloodyWormTooth(COMMON_ONE)), //血蠕虫之牙
    BLOODY_WORM_SCARF("bloody_worm_scarf", () -> new BloodyWormScarf(UNCOMMON_ONE)),
    COUNTER_SCARF("counter_scarf", () -> new CounterScarf(COMMON_ONE)),
    EVASION_SCARF("evasion_scarf", () -> new EvasionScarf(UNCOMMON_ONE)),
    HIDE_OF_ASTRUM_DEUS("hide_of_astrum_deus", () -> new AstrumDeusHide(UNCOMMON_ONE)),
    WARBANNER_SUN("warbanner_of_the_sun", () -> new SunWarBanner(UNCOMMON_ONE)),
    BRAVERY_BADGE("badge_of_bravery", () -> new BraveryBadge(COMMON_ONE)),
    ELEMENTAL_GAUNTLET("elemental_gauntlet", () -> new ElementalGauntlet(EPIC_ONE)),
    NIHILITY_QUIVER("nihility_quiver", () -> new NihilityQuiver(EPIC_ONE)),
    SCIONS_CURIO("scions_curio", () -> new ScionsCurio(COMMON_ONE)),
    DEADSHOT_BROOCH("deadshot_brooch", () -> new DeadshotBrooch(UNCOMMON_ONE)),
    DAAWNLIGHT("daawnlight_spirit_origin", () -> new DaawnlightSpiritOrigin(RARE_ONE)),
    STEM_CELLS("dynamo_stem_cells", () -> new DynamoStemCells(UNCOMMON_ONE)),
    PLANEBREAKERS_POUCH("planebreakers_pouch", () -> new PlanebreakersPouch(EPIC_ONE)),
    CONCOCTION("permafrost_concoction", () -> new PermafrostConcoction(RARE_ONE)),
    MANA_POLARIZER("mana_polarizer", () -> new ManaPolarizer(COMMON_ONE)),
    CHAOS_STONE("chaos_stone", () -> new ChaosStone(UNCOMMON_ONE)),
    CALAMITY_SIGIL("calamity_sigil", () -> new CalamitySigil(UNCOMMON_ONE)),
    ETHEREAL_TALISMAN("ethereal_talisman", () -> new EtherealTalisman(RARE_ONE)),
    CRAW_CARAPACE("craw_carapace", () -> new CrawCarapace(COMMON_ONE)),
    ABYSSAL_AMULET("abyssal_amulet", () -> new AbyssalAmulet(UNCOMMON_ONE)),
    ROTTEN_BRAIN("rotten_brain", () -> new RottenBrain(UNCOMMON_ONE)),
    SHATTERED_COMMUNITY("shattered_community", () -> new ShatteredCommunity(EPIC_ONE)),
    DARKNESS_HEART("darkness_heart", () -> new DarknessHeart(EPIC_ONE)),
    ABADDON("abaddon", () -> new Abaddon(COMMON_ONE)),
    APOLLYON("apollyon", () -> new Apollyon(UNCOMMON_ONE)),
    CALAMITY_VOID("calamity_void", () -> new CalamityVoid(UNCOMMON_ONE)),
    VEXATION_NECKLACE("vexation_necklace", () -> new VexationNecklace(COMMON_ONE)),
    APATHANULL("apathanull", () -> new Apathanull(EPIC_ONE)),
    SAND_SHARK("sand_shark_tooth_necklace", () -> new ToothNecklace(UNCOMMON_ONE, 10, 0.06f)),
    REAPER("reaper_tooth_necklace", () -> new ToothNecklace(RARE_ONE, 15, 0.15f)),
    BAROCLAW("baroclaw", () -> new Baroclaw(UNCOMMON_ONE)),
    ELDRITCH("eldritch_soul_artifact", () -> new EldritchSoulArtifact(EPIC_ONE)),
    AFFLICTION("affliction", () -> new Affliction(UNCOMMON_ONE)),
    DIMENSIONAL("dimensional_soul_artifact", () -> new DimensionalSoulArtifact(EPIC_ONE)),
    CALAMITY("calamity", () -> new Calamity(CALAMITY_ONE)),
    GIANT_SHELL("giant_shell", () -> new GiantShell(COMMON_ONE)),
    LIFE_JELLY("life_jelly", () -> new LifeJelly(COMMON_ONE)),
    CLEANSING_JELLY("cleansing_jelly", () -> new CleansingJelly(COMMON_ONE)),
    MARNITE("marnite_repulsion_shield", () -> new MarniteRepulsionShield(COMMON_ONE)),
    FUNGAL_SYMBIOTE("fungal_symbiote", () -> new FungalSymbiote(COMMON_ONE)),
    CROWN_JEWEL("crown_jewel", () -> new CrownJewel(COMMON_ONE)),
    OCEAN_SHIELD("ocean_shield", () -> new OceanShield(COMMON_ONE)),
    GIANT_PEARL("giant_pearl", () -> new GiantPearl(COMMON_ONE)),
    BEE("bee", () -> new TheBee(COMMON_ONE)),
    HONEY_DEW("honey_dew", () -> new HoneyDew(COMMON_ONE)),
    ARCHAIC_POWDER("archaic_powder", () -> new ArchaicPowder(COMMON_ONE)),
    RADIANT_OOZE("radiant_ooze", () -> new RadiantOoze(COMMON_ONE)),
    FROST_BARRIER("frost_barrier", () -> new FrostBarrier(COMMON_ONE)),
    URSA_SERGEANT("ursa_sergeant", () -> new UrsaSergeant(COMMON_ONE)),
    GIANT_TORTOISE_SHELL("giant_tortoise_shell", () -> new GiantTortoiseShell(UNCOMMON_ONE)),
    AMALGAMATED_BRAIN("amalgamated_brain", () -> new AmalgamatedBrain(UNCOMMON_ONE)),
    REGENATOR("regenator", () -> new Regenator(UNCOMMON_ONE)),
    BLOOM_STONE("bloom_stone", () -> new BloomStone(UNCOMMON_ONE)),
    LIVING_DEW("living_dew", () -> new LivingDew(UNCOMMON_ONE)),
    ANGELIC_ALLIANCE("angelic_alliance", () -> new AngelicAlliance(UNCOMMON_ONE)),
    INFECTED_JEWEL("infected_jewel", () -> new InfectedJewel(UNCOMMON_ONE)),
    BLOOD_PACT("blood_pact", () -> new BloodPact(UNCOMMON_ONE)),
    FLESH_TOTEM("flesh_totem", () -> new FleshTotem(UNCOMMON_ONE)),
    AMBROSIAL_AMPOULE("ambrosial_ampoule", () -> new AmbrosialAmpoule(RARE_ONE)),
    VITAL_JELLY("vital_jelly", () -> new VitalJelly(COMMON_ONE)),
    GRAND_GELATIN("grand_gelatin", () -> new GrandGelatin(UNCOMMON_ONE)),
    ABSORBER("absorber", () -> new Absorber(RARE_ONE)),
    DEIFIC_AMULET("deific_amulet", () -> new DeificAmulet(UNCOMMON_ONE)),
    BLAZING_CORE("blazing_core", () -> new BlazingCore(UNCOMMON_ONE)),
    BLOODFLARE_CORE("bloodflare_core", () -> new BloodflareCore(UNCOMMON_ONE)),
    EVOLUTION("evolution", () -> new Evolution(EPIC_ONE)),
    NEBULOUS_CORE("nebulous_core", () -> new NebulousCore(RARE_ONE)),
    SPONGE("sponge", () -> new Sponge(RARE_ONE)),
    ROVER_DRIVE("rover_drive", () -> new RoverDrive(UNCOMMON_ONE)),
    BLOOD_GOD_CHALICE("blood_god_chalice", () -> new BloodGodChalice(EPIC_ONE)),
    DEITIES_RAMPART("deities_rampart", () -> new DeitiesRampart(EPIC_ONE)),
    DRAEDON_HEART("draedon_heart", () -> new DraedonHeart(EPIC_ONE)),
    HARPY_RING("harpy_ring", () -> new HarpyRing(COMMON_ONE)),
    AERO_STONE("aero_stone", () -> new AeroStone(COMMON_ONE)),
    DEEP_DIVER("deep_diver", () -> new DeepDiver(RARE_ONE)),
    ORNATE_SHIELD("ornate_shield", () -> new OrnateShield(UNCOMMON_ONE)),
    ANGEL_TREADS("angel_treads", () -> new AngelTreads(UNCOMMON_ONE)),
    MOON_WALKERS("moon_walkers", () -> new MoonWalkers(RARE_ONE)),
    VOID_STRIDERS("void_striders", () -> new VoidStriders(EPIC_ONE)),
    SERAPH_TRACERS("seraph_tracers", () -> new SeraphTracers(RegisterList.CALAMITY_ONE)),
    ASGARD_VALOR("asgard_valor", () -> new AsgardValor(RARE_ONE)),
    LEVIATHAN_AMBERGRIS("leviathan_ambergris", () -> new LeviathanAmbergris(RARE_ONE)),
    GRAVISTAR_SABATON("gravistar_sabaton", () -> new GravistarSabaton(UNCOMMON_ONE)),
    GRUESOME_EMINENCE("gruesome_eminence", () -> new GruesomeEminence(CALAMITY_ONE)),
    SHIELD_OF_THE_HIGH_RULER("shield_of_the_high_ruler", () -> new ShieldOfTheHighRuler(UNCOMMON_ONE)),
    STATIS_NINJA_BELT("statis_ninja_belt", () -> new StatisNinjaBelt(RARE_ONE, false)),
    STATIS_VOID_SASH("statis_void_sash", () -> new StatisNinjaBelt(EPIC_ONE, true)),
    ELYSIAN_AEGIS("elysian_aegis", () -> new ElysianAegis(UNCOMMON_ONE)),
    ASCENDANT_INSIGNIA("ascendant_insignia", () -> new AscendantInsignia(UNCOMMON_ONE)),
    OLD_DUKE_SCALES("old_duke_scales", () -> new OldDukeScales(UNCOMMON_ONE)),
    CRIMSON_FLASK("crimson_flask", () -> new Flask(UNCOMMON_ONE, Level.NETHER, "crimson_flask", MobEffects.MOVEMENT_SLOWDOWN)),
    CORRUPT_FLASK("corrupt_flask", () -> new Flask(UNCOMMON_ONE, Level.OVERWORLD, "corrupt_flask", MobEffects.BLINDNESS)),
    ASGARDIAN_AEGIS("asgardian_aegis", () -> new AsgardianAegis(EPIC_ONE)),
    GLADIATOR_LOCKET("gladiator_locket", () -> new GladiatorLocket(UNCOMMON_ONE)),
    FROST_FLARE("frost_flare", () -> new FrostFlare(RARE_ONE)),
    COMMUNITY("community", () -> new Community(EPIC_ONE)),
    CAMPER("camper", () -> new Camper(UNCOMMON_ONE)),
    DARK_SUN_RING("dark_sun_ring", () -> new DarkSunRing(EPIC_ONE)),
    AMALGAM("amalgam", () -> new Amalgam(EPIC_ONE)),
    COIN_OF_DECEIT("coin_of_deceit", () -> new CoinOfDeceit(COMMON_ONE)),
    SCUTTLERS_JEWEL("scuttlers_jewel", () -> new ScuttlersJewel(COMMON_ONE)),
    RAIDERS_TALISMAN("raiders_talisman", () -> new RaidersTalisman(COMMON_ONE)),
    ROTTEN_DOG_TOOTH("rotten_dog_tooth", () -> new RottenDogTooth(COMMON_ONE)),
    INK_BOMB("ink_bomb", () -> new InkBomb(UNCOMMON_ONE)),
    SILENCING_SHEATH("silencing_sheath", () -> new SilencingSheath(COMMON_ONE)),
    BLOODSTAINED_GLOVE("bloodstained_glove", () -> new BloodstainedGlove(COMMON_ONE)),
    FILTHY_GLOVE("filthy_glove", () -> new FilthyGlove(COMMON_ONE)),
    FEATHER_CROWN("feather_crown", () -> new FeatherCrown(COMMON_ONE)),
    MIRAGE_MIRROR("mirage_mirror", () -> new MirageMirror(COMMON_ONE)),
    CORROSIVE_SPINE("corrosive_spine", () -> new CorrosiveSpine(COMMON_ONE)),
    ELECTRICIANS_GLOVE("electricians_glove", () -> new ElectriciansGlove(UNCOMMON_ONE)),
    RUIN_MEDALLION("ruin_medallion", () -> new RuinMedallion(UNCOMMON_ONE)),
    VAMPIRIC_TALISMAN("vampiric_talisman", () -> new VampiricTalisman(UNCOMMON_ONE)),
    PRECISION_GLOVE("precision_glove", () -> new PrecisionGlove(COMMON_ONE)),
    RECKLESSNESS_GLOVE("recklessness_glove", () -> new RecklessnessGlove(COMMON_ONE)),
    ABYSSAL_MIRROR("abyssal_mirror", () -> new AbyssalMirror(RARE_ONE)),
    ETHEREAL_EXTORTER("ethereal_extorter", () -> new EtherealExtorter(UNCOMMON_ONE)),
    PLAGUE_FUEL_PACK("plagued_fuel_pack", () -> new PlagueFuelPack(RARE_ONE)),
    DARK_MATTER_SHEATH("dark_matter_sheath", () -> new DarkMatterSheath(RARE_ONE)),
    MOONSTONE_CROWN("moonstone_crown", () -> new MoonstoneCrown(RARE_ONE)),
    BLUNDER_BOOSTER("blunder_booster", () -> new BlunderBooster(EPIC_ONE)),
    SPECTRAL_VEIL("spectral_veil", () -> new SpectralVeil(RARE_ONE)),
    VENERATED_LOCKET("venerated_locket", () -> new VeneratedLocket(EPIC_ONE)),
    ORACLE_DECK("oracle_deck", OracleDeck::new),
    FATE_THREAD("fate_thread", () -> new UnsealingRope("fate_thread")),
    ABYSS_THREAD("abyss_thread", () -> new UnsealingRope("abyss_thread")),
    BRILLIANCE("brilliance", () -> new Brilliance(UNCOMMON_ONE)),
    AURA("aura", () -> new Aura(UNCOMMON_ONE)),
    INSPIRATION("inspiration", () -> new Inspiration(UNCOMMON_ONE)),
    ENDURANCE("endurance", () -> new Endurance(UNCOMMON_ONE)),
    ENTITY("entity", () -> new EntityCard(UNCOMMON_ONE)),
    WISDOM("wisdom", () -> new Wisdom(UNCOMMON_ONE)),
    METROPOLIS("metropolis", () -> new Metropolis(UNCOMMON_ONE)),
    RADIANCE_CARD("radiance_card", () -> new RadianceCard(UNCOMMON_ONE)),
    TEMPERANCE("temperance", () -> new Temperance(UNCOMMON_ONE)),
    WYRM_TOOTH_NECKLACE("wyrm_tooth_necklace", () -> new WyrmToothNecklace(CALAMITY_ONE)),
    DEUS_CORE("deus_core", () -> new DeusCore(EPIC_ONE)),
    FORESEE_ORB("foresee_orb", () -> new ForeseeOrb(RARE_ONE)),
    SACRIFICES_MASK("sacrifices_mask", () -> new SacrificesMask(UNCOMMON_ONE)),
    TAINTED_DECK("tainted_deck", TaintedDeck::new),
    CONFUSE("confuse", () -> new Confuse(EPIC_ONE)),
    BARREN("barren", () -> new Barren(EPIC_ONE)),
    FRAIL("frail", () -> new Frail(EPIC_ONE)),
    NOTHING("nothing", () -> new Nothing(EPIC_ONE)),
    GREED("greed", () -> new Greed(EPIC_ONE)),
    FOOL("fool", () -> new Fool(EPIC_ONE)),
    TARNISH("tarnish", () -> new Tarnish(EPIC_ONE)),
    PERPLEXED("perplexed", () -> new Perplexed(EPIC_ONE)),
    SACRIFICE("sacrifice", () -> new Sacrifice(EPIC_ONE)),
    HOLY_MOONLIGHT("holy_moonlight", () -> new HolyMoonlight(EPIC_ONE)),
    STAR_CHARM("star_charm", () -> new StarCharm(UNCOMMON_ONE)),
    FUNGAL_CLUMP("fungal_clump", () -> new FungalClump(UNCOMMON_ONE)),
    ODINS_REFUGE("odins_refuge", () -> new OdinsRefuge(EPIC_ONE)),
    SILVAS_CROWN("silvas_crown", () -> new SilvasCrown(UNCOMMON_ONE)),
    NIHILITY_SHELL("nihility_shell", () -> new NihilityShell(EPIC_ONE)),
    AZURE_ABYSS_TALISMAN("azure_abyss_talisman", () -> new AzureAbyssTalisman(RARE_ONE)),
    ARC_FLASH_RING("arc_flash_ring", () -> new ArcFlashRing(RARE_ONE)),
    ECLIPSE_MIRROR("eclipse_mirror", EclipseMirror::new),
    DEITY_MASK("deity_mask", NamelessDeityMask::new),
    AVATAR_MASK("avatar_mask", EmptinessAvatarMask::new),
    PORTAL_SKIRT("portal_skirt", PortalSkirt::new),
    HYPERPLANE_MATRIX("hyperplane_matrix", HyperplaneMatrix::new),
    PURITY("purity", Purity::new),
    OCEAN_FLOWER("ocean_flower", OceanFlower::new),

    //Item
    COMET_SHARD(() -> new MagicItem(RARE_ONE, 0), "comet_shard"),

    ETHEREAL_CORE(() -> new MagicItem(UNCOMMON_ONE, 1), "ethereal_core"),

    PHANTOM_HEART(() -> new MagicItem(EPIC_ONE, 2), "phantom_heart"),

    ENCHANTED_STARFISH(EnchantedStarfish::new, "enchanted_starfish"),

    SUPREME_MANA_POTION(SupremeManaPotion::new, "supreme_mana_potion"),

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

    MELD_CONSTRUCT(() -> new Item(ITEM_UNCOMMON), "meld_construct"),

    ELECTROLYTE_GEL_PACK(() -> new AdrenalineItem(RARE_ONE, 0), "electrolyte_gel_pack"),

    STARLIGHT_FUEL_CELL(() -> new AdrenalineItem(UNCOMMON_ONE, 1), "starlight_fuel_cell"),

    ECTOHEART(() -> new AdrenalineItem(EPIC_ONE, 2), "ectoheart"),

    CELESTIAL_ONION(() -> new CelestialOnion(EPIC_ONE), "celestial_onion"),

    SANGUINE_TANGERINE(() -> new LifeFruit(Rarity.UNCOMMON, 1), "sanguine_tangerine"),

    MIRACLE_FRUIT(() -> new LifeFruit(Rarity.RARE, 2), "miracle_fruit"),

    TAINTED_CLOUDBERRY(() -> new LifeFruit(Rarity.EPIC, 3), "tainted_cloudberry"),

    SACRED_STRAWBERRY(() -> new LifeFruit(CALAMITY_RARITY, 4), "sacred_strawberry"),

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

    BRIMSTONE_LOCUS(() -> new BrimstoneLocus(RegisterList.CALAMITY_ONE, "brimstone_locus", 1), "brimstone_locus"),

    PERVERSE_PURSE(() -> new PerversePurse(UNCOMMON_ONE), "perverse_purse"),

    DEMON_SHADE_HELMET(() -> new DemonShade(ArmorItem.Type.HELMET), "demon_shade_helmet"),

    DEMON_SHADE_CHEST(() -> new DemonShade(ArmorItem.Type.CHESTPLATE), "demon_shade_chest"),

    DEMON_SHADE_LEGGINGS(() -> new DemonShade(ArmorItem.Type.LEGGINGS), "demon_shade_leggings"),

    DEMON_SHADE_BOOTS(() -> new DemonShade(ArmorItem.Type.BOOTS), "demon_shade_boots"),

    SHADOW_SPEC_BAR(ShadowSpecBar::new, "shadow_spec_bar"),

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

    EMPTINESS_SPRAYER(EmptinessSprayer::new, "emptiness_sprayer"),

    INFERNAL_SUEVITE_ORE(() -> new BlockItem(CalamityLightBlock.INFERNAL_SUEVITE_ORE.get(), FIRE_RESISTANT_ORE), "infernal_suevite_ore"),

    INFERNAL_SUEVITE(() -> new Item(FIRE_RESISTANT_ORE), "infernal_suevite"),

    EXODIUM_CLUSTER(() -> new TooltipItem(ITEM_EPIC, "exodium_cluster"), "exodium_cluster"),

    EXODIUM_CLUSTER_ORE(() -> new BlockItem(CalamityLightBlock.EXODIUM_CLUSTER_ORE.get(), ITEM_EPIC), "exodium_cluster_ore"),

    BODY(Body::new, "ancient_fragments_body"),

    ECHO(Echo::new, "ancient_fragments_echo"),

    MIND(Mind::new, "ancient_fragments_mind"),

    SOUL(Soul::new, "ancient_fragments_soul"),

    THORNY(Thorny::new, "rune_stone_thorny"),

    SHINING(Shining::new, "rune_stone_shining"),

    HARD(() -> new Defense("hard", .1), "rune_stone_hard"),

    REGEN(Regen::new, "rune_stone_regen"),

    LIGHT(Light::new, "rune_stone_light"),

    SILENCE(Silence::new, "rune_stone_silence"),

    GUARDED(() -> new Defense("guarded", .15), "rune_stone_guarded"),

    BIOCHEMISTRY(Biochemistry::new, "rune_stone_biochemistry"),

    MAGICAL(Magical::new, "enchanted_scroll_magical"),

    RECKLESS(Reckless::new, "enchanted_scroll_reckless"),

    EVOKER(Evoker::new, "enchanted_scroll_evoker"),

    MIRACLE(Miracle::new, "enchanted_scroll_miracle"),

    MASSIVE(Massive::new, "enchanted_scroll_massive"),

    DESTINED_GREATNESS(DestinedGreatness::new, "origin_gem_destined_greatness"),

    GREAT(Great::new, "origin_gem_great"),

    SACRIFICAL(Sacrifical::new, "origin_gem_sacrifical"),

    WIZARD(Wizard::new, "origin_gem_wizard"),

    GOD_FORGED(GodForged::new, "origin_gem_god_forged"),

    VOID(Void::new, "blessing_void"),

    VOID_TOUCHED(VoidTouched::new, "blessing_void_touched"),

    END(End::new, "blessing_end"),

    LAST_STAND(LastStand::new, "blessing_last_stand"),

    HEAT_DEATH(HeatDeath::new, "blessing_heat_death"),

    PREFIX_CLEAR_KNIFE(() -> new TooltipItem(UNCOMMON_ONE, "prefix_clear_knife"), "prefix_clear_knife"),

    HAVOC_ESSENCE(HavocEssence::new, "havoc_essence"),

    ELEUM_ESSENCE(EleumEssence::new, "eleum_essence"),

    CALAMITY_CORE(() -> new TooltipItem(ITEM_UNCOMMON, "calamity_core"), "calamity_core"),

    RUINOUS_SOUL(RuinousSoul::new, "ruinous_soul"),

    SCORIA_INGOT(() -> new TooltipItem(ITEM_UNCOMMON, "scoria_ingot"), "scoria_ingot"),

    UNHOLY_CORE(() -> new TooltipItem(ITEM_UNCOMMON, "unholy_core"), "unholy_core"),

    //SwordAndTool
    ETERNITY(Eternity::new, "eternity"),

    NEBULOUS_CATACLYSM(() -> new NebulousCataclysm(EPIC_ONE), "nebulous_cataclysm"),

    EXCELSUS(() -> new Excelsus(14, -2f, EPIC_ONE), "excelsus"),

    ATARAXIA(() -> new Ataraxia(11, -2f, EPIC_ONE), "ataraxia"),

    UNIVERSE_SPLITTER(() -> new UniverseSplitter(EPIC_ONE), "universe_splitter"),

    YHARIMS_CRYSTAL(() -> new YharimsCrystal(RegisterList.CALAMITY_ONE), "yharims_crystal"),

    CRYSTYL_CRUSHER(() -> new CrystylCrusher(EPIC_ONE), "crystyl_crusher"),

    DREAM_CATCHER(() -> new DreamCatcher(EPIC_ONE), "dream_catcher"),

    ANTI_VOID(() -> new AntiVoid(EPIC_ONE), "anti_void"),

    STARLESS_NIGHT(() -> new StarlessNight(RARE_ONE), "starless_night"),

    XYTHERON(Xytheron::new, "xytheron"),

    STORM_MAIDENS_RETRIBUTION(() -> new StormMaidensRetribution(RegisterList.CALAMITY_ONE), "storm_maidens_retribution"),

    ICE_SNOW_MIRROR(IceSnowMirror::new, "ice_mirror"),

    BRIMSTONE_BARRIER(BrimstoneCrescentStaff::new, "brimstone_crescent_staff"),

    SEAFOAM_BOMB(SeafoamBomb::new, "seafoam_bomb"),

    METALLIC_CHUNK(MetallicChunk::new, "metallic_chunk"),

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