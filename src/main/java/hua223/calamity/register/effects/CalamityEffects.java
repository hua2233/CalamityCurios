package hua223.calamity.register.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import static hua223.calamity.register.RegisterList.EFFECTS;

public class CalamityEffects {
    public static final RegistryObject<MobEffect> ASTRAL_INFECTION = EFFECTS.register("astral_infection",
        () -> new AstralInfection(MobEffectCategory.HARMFUL, 0x2E9992));

    public static final RegistryObject<MobEffect> CURSED_INFERNO = EFFECTS.register("cursed_inferno",
        () -> new CursedInferno(MobEffectCategory.HARMFUL, 0x37C81A));

    public static final RegistryObject<MobEffect> ELEMENTAL_MIX = EFFECTS.register("elemental_mix",
        () -> new ElementalMix(MobEffectCategory.HARMFUL, 0xEF00FF));

    public static final RegistryObject<MobEffect> IRRADIATED = EFFECTS.register("irradiated",
        () -> new Irradiated(MobEffectCategory.HARMFUL, 0x85B431));

    public static final RegistryObject<MobEffect> ELECTRIFIED = EFFECTS.register("electrified",
        () -> new Electrified(MobEffectCategory.HARMFUL, 0xFFB000));

    public static final RegistryObject<MobEffect> FREEZE = EFFECTS.register("freeze",
        () -> new Freeze(MobEffectCategory.NEUTRAL, 0x0000FF));

    public static final RegistryObject<MobEffect> MANA_BURN = EFFECTS.register("mana_burn",
        () -> new MagicBurn(MobEffectCategory.HARMFUL, 0xDC143C));

    public static final RegistryObject<MobEffect> MANA_SICKNESS = EFFECTS.register("mana_sickness",
        () -> new ManaSickness(MobEffectCategory.HARMFUL, 0x7D55FF));

    public static final RegistryObject<MobEffect> CRUMBLING = EFFECTS.register("crumbling",
        () -> new Crumbling(MobEffectCategory.HARMFUL, 0xA9A9A9));

    public static final RegistryObject<MobEffect> RIPTIDE = EFFECTS.register("riptide",
        () -> new Riptide(MobEffectCategory.HARMFUL, 0x87CEEB));

    public static final RegistryObject<MobEffect> BRIMSTONE_FLAMES = EFFECTS.register("brimstone_flames",
        () -> new BrimstoneFlames(MobEffectCategory.HARMFUL, 0x3F0710));

    public static final RegistryObject<MobEffect> CRUSH_DEPTH = EFFECTS.register("crush_depth",
        () -> new CrushDepth(MobEffectCategory.HARMFUL, 0x0E2C47));

    public static final RegistryObject<MobEffect> DRAGON_BURN = EFFECTS.register("dragon_burn",
        () -> new DragonBurn(MobEffectCategory.HARMFUL, 0xF21818));

    public static final RegistryObject<MobEffect> ACID_VENOM = EFFECTS.register("acid_venom",
        () -> new AcidVenom(MobEffectCategory.HARMFUL, 0xBC7DBE));

    public static final RegistryObject<MobEffect> WHISPERING_DEATH = EFFECTS.register("whispering_death",
        () -> new WhisperingDeath(MobEffectCategory.HARMFUL, 0x640D1D));

    public static final RegistryObject<MobEffect> MUSHY = EFFECTS.register("mushy",
        () -> new Mushy(MobEffectCategory.BENEFICIAL, 0xA0D4F5));

    public static final RegistryObject<MobEffect> GLACIAL_STATE = EFFECTS.register("glacial_state",
        () -> new GlacialState(MobEffectCategory.HARMFUL, 0x3E829F));

    public static final RegistryObject<MobEffect> CONFUSED = EFFECTS.register("confused",
        () -> new Confused(MobEffectCategory.HARMFUL, 0x8B008B));

    public static final RegistryObject<MobEffect> RE_GENA = EFFECTS.register("re_gena",
        () -> new ReGena(MobEffectCategory.BENEFICIAL, 0x18E215));

    public static final RegistryObject<MobEffect> NATURE_PAIN = EFFECTS.register("nature_pain",
        () -> new NaturePain(MobEffectCategory.HARMFUL, 0x00563B));

    public static final RegistryObject<MobEffect> GOD_SLAYER_INFERNO = EFFECTS.register("god_slayer_inferno",
        () -> new GodSlayerInferno(MobEffectCategory.HARMFUL, 0xBC28C9));

    public static final RegistryObject<MobEffect> GRUESOME_EVIL_SPIRITS = EFFECTS.register("gruesome_evil_spirits",
        () -> new GruesomeEvilSpirits(MobEffectCategory.NEUTRAL, 11141290));

    public static final RegistryObject<MobEffect> DODGE_CD = EFFECTS.register("dodge_cd",
        () -> new DodgeCD(MobEffectCategory.NEUTRAL, 0xD3D3D3));

    public static final RegistryObject<MobEffect> VULNERABILITY_HEX = EFFECTS.register("vulnerability_hex",
        () -> new VulnerabilityHex(MobEffectCategory.HARMFUL, 0x612216));

    public static final RegistryObject<MobEffect> APOPTOSIS = EFFECTS.register("apoptosis", () ->
        new Apoptosis(MobEffectCategory.NEUTRAL, 0xffffff));

    public static final RegistryObject<MobEffect> EUTROPHICATION = EFFECTS.register("eutrophication",
        () -> new Eutrophication(MobEffectCategory.HARMFUL, 0x0B1F44));

    public static final RegistryObject<MobEffect> PLAGUE = EFFECTS.register("plague",
        () -> new Plague(MobEffectCategory.HARMFUL, 0x4D79FF));

    public static final RegistryObject<MobEffect> PLENTY_SATISFIED = EFFECTS.register("plenty_satisfied",
        () -> new PlentySatisfied(MobEffectCategory.BENEFICIAL, 16777045));

    public static final RegistryObject<MobEffect> MANA_REGENERATION = EFFECTS.register("mana_regeneration",
        () -> new ManaRegeneration(MobEffectCategory.BENEFICIAL, 5111160));

    public static final RegistryObject<MobEffect> MAGIC_POWER = EFFECTS.register("magic_power",
        () -> new MagicPower(MobEffectCategory.BENEFICIAL, 15171136));

    public static final RegistryObject<MobEffect> GRAPE_BEER = EFFECTS.register("grape_beer",
        () -> new GrapeBeer(MobEffectCategory.HARMFUL, 11141290));

    public static final RegistryObject<MobEffect> MARGARITA = EFFECTS.register("margarita",
        () -> new Margarita(MobEffectCategory.HARMFUL, 12238227));

    public static final RegistryObject<MobEffect> WHITE_WINE = EFFECTS.register("white_wine",
        () -> new WhiteWine(MobEffectCategory.NEUTRAL, 16777215));

    public static final RegistryObject<MobEffect> TRIPPY = EFFECTS.register("trippy",
        () -> new Trippy(MobEffectCategory.NEUTRAL, 0xFF0000));

    public static final RegistryObject<MobEffect> BAGUETTE = EFFECTS.register("baguette",
        () -> new Baguette(MobEffectCategory.BENEFICIAL, 13794414));

    public static final RegistryObject<MobEffect> WELL_FED = EFFECTS.register("well_fed",
        () -> new EatingWell(MobEffectCategory.BENEFICIAL, 16777045));

    public static final RegistryObject<MobEffect> EXQUISITELY_STUFFED = EFFECTS.register("exquisitely_stuffed",
        () -> new ExquisitelyStuffed(MobEffectCategory.BENEFICIAL, 16777045));

    public static final RegistryObject<MobEffect> ANECHOIC_COATING = EFFECTS.register("anechoic_coating",
        () -> new AnechoicCoating(MobEffectCategory.BENEFICIAL, 43690));

    public static final RegistryObject<MobEffect> BOUNDING = EFFECTS.register("bounding",
        () -> new Bounding(MobEffectCategory.BENEFICIAL, 3255451));

    public static final RegistryObject<MobEffect> CALCIUM = EFFECTS.register("calcium",
        () -> new Calcium(MobEffectCategory.BENEFICIAL, 9801814));

    public static final RegistryObject<MobEffect> TESLA = EFFECTS.register("tesla",
        () -> new Tesla(MobEffectCategory.BENEFICIAL, 5636095));

    public static final RegistryObject<MobEffect> GALVANIC_CORROSION = EFFECTS.register("galvanic_corrosion",
        () -> new GalvanicCorrosion(MobEffectCategory.HARMFUL, 5636095));

    public static final RegistryObject<MobEffect> ZEN = EFFECTS.register("zen",
        () -> new Zen(MobEffectCategory.NEUTRAL, 16777215));

    public static final RegistryObject<MobEffect> ZERG = EFFECTS.register("zerg",
        () -> new Zerg(MobEffectCategory.NEUTRAL, 11141290));

    public static final RegistryObject<MobEffect> PHOTOSYNTHESIS = EFFECTS.register("photosynthesis",
        () -> new Photosynthesis(MobEffectCategory.BENEFICIAL, 16777045));

    public static final RegistryObject<MobEffect> ASTRAL_INJECTION = EFFECTS.register("astral_injection",
        () -> new AstralInjection(MobEffectCategory.NEUTRAL, 5636095));

    public static final RegistryObject<MobEffect> CEASELESS_HUNGER = EFFECTS.register("ceaseless_hunger",
        () -> new CeaselessHunger(MobEffectCategory.NEUTRAL, 16733695));

    public static final RegistryObject<MobEffect> OMNISCIENCE = EFFECTS.register("omniscience",
        () -> new Omniscience(MobEffectCategory.NEUTRAL, 16733695));

    public static final RegistryObject<MobEffect> STAR_STRIKINGLY_SATIATED = EFFECTS.register("star_strikingly_satiated",
        () -> new StarStrikinglySatiated(MobEffectCategory.NEUTRAL, 0xF38BBC));

    public static final RegistryObject<MobEffect> CHAOS_STATE = EFFECTS.register("chaos_state",
        () -> new ChaosState(MobEffectCategory.NEUTRAL, 16733525));

    public static final RegistryObject<MobEffect> DECEIVE = EFFECTS.register("deceive",
        () -> new Deceive(MobEffectCategory.HARMFUL, 16733525));

    public static final RegistryObject<MobEffect> VOID_TOUCH = EFFECTS.register("void_touch",
        () -> new VoidTouch(MobEffectCategory.HARMFUL, 11141290));

    public static final RegistryObject<MobEffect> MOONLIGHT_SHIELD = EFFECTS.register("moonlight_shield",
        () -> new MoonlightShield(MobEffectCategory.BENEFICIAL, 16733695));

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);

    }
}
