package hua223.calamity.generators.tag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.Items.CalamityItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static hua223.calamity.register.Items.CalamityItems.*;
import static top.theillusivec4.curios.api.CuriosApi.MODID;

//我真是服了，手写老是忘记了..... 干脆序列化得了
public class CuriosTag extends JsonCodecProvider<CuriosTag.CuriosTagData> {
    public CuriosTag(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, existingFileHelper, "calamity_curios", JsonOps.INSTANCE,
            PackType.SERVER_DATA, "tags/items", CuriosTagData.codec(), getTagEnters());
    }

    private static Map<ResourceLocation, CuriosTagData> getTagEnters() {
        final Map<ResourceLocation, CuriosTagData> ENTRIES = new HashMap<>();
        final BiConsumer<String, List<CalamityItems>> factory = (type, list) ->
            ENTRIES.put(CalamityCurios.ResourceOf(MODID, type), new CuriosTagData(list));

        factory.accept("head", List.of(
            ROTTEN_BRAIN, ABADDON, ELDRITCH, FUNGAL_SYMBIOTE,
            URSA_SERGEANT, AMALGAMATED_BRAIN, AMALGAM, BLOOD_PACT,
            BLOODFLARE_CORE, ASCENDANT_INSIGNIA, FEATHER_CROWN,
            RUIN_MEDALLION, ETHEREAL_EXTORTER, MOONSTONE_CROWN,
            SACRIFICES_MASK));

        factory.accept("necklace", List.of(
            YHARIM_GIFT, COUNTER_SCARF, BLOODY_WORM_SCARF,
            EVASION_SCARF, RUSTY_MEDALLION, DEADSHOT_BROOCH,
            CHAOS_STONE, VEXATION_NECKLACE, SAND_SHARK,
            REGENATOR, REAPER, LIFE_JELLY, BEE, AMBROSIAL_AMPOULE,
            BLAZING_CORE, EVOLUTION, GRAVISTAR_SABATON,
            GLADIATOR_LOCKET, RAIDERS_TALISMAN, VENERATED_LOCKET,
            WYRM_TOOTH_NECKLACE, FORESEE_ORB, HOLY_MOONLIGHT));

        factory.accept("charm", List.of(
            HIDE_OF_ASTRUM_DEUS, RADIANCE, BADGE_BRAVERY,
            DAAWNLIGHT, CONCOCTION, CALAMITY_SIGIL,
            ABYSSAL_AMULET, ETHEREAL_TALISMAN, SHATTERED_COMMUNITY,
            COMMUNITY, DIMENSIONAL, CLEANSING_JELLY, FROST_BARRIER,
            BLOOM_STONE, INFECTED_JEWEL, DEIFIC_AMULET,
            AERO_STONE, ANGEL_TREADS, VAMPIRIC_TALISMAN, STAR_CHARM));

        factory.accept("back", List.of(
            WARBANNER_SUN, NIHILITY_QUIVER, ELEMENTAL_QUIVER,
            CALAMITY_VOID, GIANT_SHELL, GIANT_TORTOISE_SHELL,
            MARNITE, DEEP_DIVER, LEVIATHAN_AMBERGRIS,
            CAMPER, INK_BOMB, SILENCING_SHEATH, CORROSIVE_SPINE,
            PLAGUE_FUEL_PACK, BLUNDER_BOOSTER, SPECTRAL_VEIL));

        factory.accept("hands", List.of(
            BLOODY_WORM_TOOTH, ELEMENTAL_GAUNTLET, CRAW_CARAPACE,
            DESTINY_BOOK, BAROCLAW, OCEAN_SHIELD, DARK_MATTER_SHEATH,
            ARCHAIC_POWDER, RADIANT_OOZE, ANGELIC_ALLIANCE,
            FLESH_TOTEM, VITAL_JELLY, GRAND_GELATIN,
            ABSORBER, BLOOD_GOD_CHALICE, DEITIES_RAMPART,
            ORNATE_SHIELD, ASGARD_VALOR, GRUESOME_EMINENCE,
            SHIELD_OF_THE_HIGH_RULER, ELYSIAN_AEGIS, OLD_DUKE_SCALES,
            ASGARDIAN_AEGIS, FROST_FLARE, COIN_OF_DECEIT,
            SCUTTLERS_JEWEL, ROTTEN_DOG_TOOTH, BLOODSTAINED_GLOVE,
            FILTHY_GLOVE, MIRAGE_MIRROR, ELECTRICIANS_GLOVE,
            PRECISION_GLOVE, RECKLESSNESS_GLOVE, ABYSSAL_MIRROR));

        factory.accept("body", List.of(
            STEM_CELLS, MANA_POLARIZER, DARKNESS_HEART,
            AFFLICTION, NEBULOUS_CORE, DRAEDON_HEART,
            SPONGE, DEUS_CORE));

        factory.accept("belt", List.of(
            STATIS_NINJA_BELT, STATIS_VOID_SASH));

        factory.accept("ring", List.of(
            EXTINCTION_VOID, CROWN_JEWEL, GIANT_PEARL,
            HONEY_DEW, LIVING_DEW, DARK_SUN_RING,
            CORRUPT_FLASK, CRIMSON_FLASK, HARPY_RING));

        factory.accept("cards", List.of(
            ORACLE_DECK, BRILLIANCE, AURA,
            INSPIRATION, ENDURANCE, ENTITY,
            WISDOM, METROPOLIS, RADIANCE_CARD,
            TEMPERANCE, TAINTED_DECK, CONFUSE,
            BARREN, FRAIL, NOTHING, GREED, FOOL,
            TARNISH, PERPLEXED, SACRIFICE
        ));

        factory.accept("spellbook", List.of(
            ETERNITY));

        return ENTRIES;
    }

    public record CuriosTagData(List<CalamityItems> curioId) {
        static Codec<CuriosTagData> codec() {
            return RecordCodecBuilder.create(instance ->
                instance.group(ResourceLocation.CODEC.listOf().fieldOf("values")
                    .forGetter(CuriosTagData::toSerialization)).apply(instance, CuriosTagData::deserialization));
        }

        //No need for deserialization, Only used for generating JSON, will be excluded from packaging
        private static CuriosTagData deserialization(List<ResourceLocation> item) {
            throw new UnsupportedOperationException("Deserialization not supported");
        }

        private List<ResourceLocation> toSerialization() {
            return curioId.stream().map(CalamityItems::getId).toList();
        }
    }
}
