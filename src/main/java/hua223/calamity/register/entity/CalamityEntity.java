package hua223.calamity.register.entity;

import hua223.calamity.register.entity.projectiles.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import static hua223.calamity.register.RegisterList.ENTITIES;

//Most of the entities' dependencies are set manually, so they shouldn't be summoned by commands
public class CalamityEntity {
    public static final RegistryObject<EntityType<ZenithProjectile>> ZENITH_PROJECTILE = ENTITIES.register("zenith_projectile",
        () -> EntityType.Builder.of(ZenithProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(32)
            .updateInterval(-1).build("zenith_projectile"));

    public static final RegistryObject<EntityType<Meteor>> METEOR = ENTITIES.register("meteor",
        () -> EntityType.Builder.of(Meteor::new, MobCategory.MISC).sized(0.2f, 0.2f)
            .clientTrackingRange(8).updateInterval(1).noSummon().build("meteor"));

    public static final RegistryObject<EntityType<FireMeteor>> FIRE_METEOR = ENTITIES.register("fire_meteor",
        () -> EntityType.Builder.of(FireMeteor::new, MobCategory.MISC).sized(0.6f, 0.6f)
            .clientTrackingRange(8).updateInterval(1).noSummon().build("fire_meteor"));

    public static final RegistryObject<EntityType<AcidicRain>> ACIDIC_RAIN = ENTITIES.register("acidic_rain",
        () -> EntityType.Builder.of(AcidicRain::new, MobCategory.MISC).sized(0.2f, 0.2f)
            .clientTrackingRange(8).updateInterval(1).noSummon().build("acidic_rain"));

    public static final RegistryObject<EntityType<MiniDragonBorn>> MINI_DRAGON = ENTITIES.register("mini_dragon_born",
        () -> EntityType.Builder.of(MiniDragonBorn::new, MobCategory.MISC).sized(0.5f, 0.5f)
            .clientTrackingRange(12).updateInterval(1).noSummon().build("mini_dragon_born"));

    public static final RegistryObject<EntityType<ShadowsRain>> SHADOWS_RAIN = ENTITIES.register("shadows_rain",
        () -> EntityType.Builder.of(ShadowsRain::new, MobCategory.MISC).sized(0.2f, 0.2f)
            .clientTrackingRange(12).updateInterval(1).noSummon().build("shadows_rain"));

    public static final RegistryObject<EntityType<Nebula>> NEBULA = ENTITIES.register("nebula",
        () -> EntityType.Builder.of(Nebula::new, MobCategory.MISC).sized(0.1f, 0.1f)
            .clientTrackingRange(6).updateInterval(1).noSummon().build("nebula"));

    public static final RegistryObject<EntityType<RancorMagicCircle>> RANCOR = ENTITIES.register("rancor",
        () -> EntityType.Builder.of(RancorMagicCircle::new, MobCategory.MISC).sized(0.1f, 0.1f)
            .clientTrackingRange(32).updateInterval(1).noSummon().noSave().build("rancor"));

    public static final RegistryObject<EntityType<RancorLaserBeam>> RANCOR_LASER = ENTITIES.register("rancor_laser",
        () -> EntityType.Builder.of(RancorLaserBeam::new, MobCategory.MISC).sized(0.1f, 0.1f)
            .clientTrackingRange(32).updateInterval(1).noSummon().noSave().build("rancor_laser"));

    public static final RegistryObject<EntityType<UniverseSplitterField>> USF = ENTITIES.register("universe_splitter_field",
        () -> EntityType.Builder.of(UniverseSplitterField::new, MobCategory.MISC).sized(0f, 0f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("universe_splitter_field"));

    public static final RegistryObject<EntityType<UniverseSplitterSmallBeam>> USB = ENTITIES.register("universe_splitter_small_beam",
        () -> EntityType.Builder.of(UniverseSplitterSmallBeam::new, MobCategory.MISC).sized(1f, 40f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("universe_splitter_small_beam"));

    public static final RegistryObject<EntityType<UniverseSplitterHugeBeam>> USH = ENTITIES.register("universe_splitter_huge_beam",
        () -> EntityType.Builder.of(UniverseSplitterHugeBeam::new, MobCategory.MISC).sized(0f, 0f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("universe_splitter_huge_beam"));

    public static final RegistryObject<EntityType<EternityHex>> ETERNITY_HEX = ENTITIES.register("eternity_hex",
        () -> EntityType.Builder.of(EternityHex::new, MobCategory.MISC).sized(1f, 0.5f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("eternity_hex"));

    public static final RegistryObject<EntityType<ExcelsusBlue>> EXCELSUS_BLUE = ENTITIES.register("excelsus_blue",
        () -> EntityType.Builder.of(ExcelsusBlue::new, MobCategory.MISC).sized(1f, 1.5f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("excelsus_blue"));

    public static final RegistryObject<EntityType<ExcelsusMain>> EXCELSUS_MAIN = ENTITIES.register("excelsus_main",
        () -> EntityType.Builder.of(ExcelsusMain::new, MobCategory.MISC).sized(1f, 1.5f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("excelsus_main"));

    public static final RegistryObject<EntityType<ExcelsusPink>> EXCELSUS_PINK = ENTITIES.register("excelsus_pink",
        () -> EntityType.Builder.of(ExcelsusPink::new, MobCategory.MISC).sized(1f, 1.5f)
            .clientTrackingRange(16).updateInterval(1).noSummon().noSave().build("excelsus_pink"));

    public static final RegistryObject<EntityType<NebulaCloudCore>> NEBULA_CLOUD_CORE = ENTITIES.register("nebula_cloud_core",
        () -> EntityType.Builder.of(NebulaCloudCore::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(32).updateInterval(1).noSummon().noSave().build("nebula_cloud_core"));

    public static final RegistryObject<EntityType<NebulaNova>> NEBULA_NOVA = ENTITIES.register("nebula_nova",
        () -> EntityType.Builder.of(NebulaNova::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(32).updateInterval(1).noSummon().noSave().build("nebula_nova"));

    public static final RegistryObject<EntityType<GladiatorHealOrb>> HEAL_ORB = ENTITIES.register("heal_orb",
        () -> EntityType.Builder.of(GladiatorHealOrb::new, MobCategory.MISC).sized(0.3f, 0.3f)
            .clientTrackingRange(32).updateInterval(1).noSummon().noSave().build("heal_orb"));

    public static final RegistryObject<EntityType<JewelSpike>> JEWEL_SPIKE = ENTITIES.register("jewel_spike",
        () -> EntityType.Builder.of(JewelSpike::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(16).updateInterval(20)
            .noSummon().noSave().setShouldReceiveVelocityUpdates(false).build("jewel_spike"));

    public static final RegistryObject<EntityType<TeslaAura>> TESLA_AURA = ENTITIES.register("tesla_aura",
        () -> EntityType.Builder.of(TeslaAura::new, MobCategory.MISC).sized(5f, 0.2f)
            .clientTrackingRange(16).updateInterval(3)
            .noSummon().noSave().setShouldReceiveVelocityUpdates(false).build("tesla_aura"));

    public static final RegistryObject<EntityType<LunarFlare>> LUNAR_FLARE = ENTITIES.register("lunar_flare",
        () -> EntityType.Builder.of(LunarFlare::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(16).updateInterval(2)
            .noSummon().noSave().setShouldReceiveVelocityUpdates(false).build("lunar_flare"));

    public static final RegistryObject<EntityType<BlackHolePet>> BLACK_HOLE = ENTITIES.register("black_hole",
        () -> EntityType.Builder.of(BlackHolePet::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(16).updateInterval(2)
            .noSave().setShouldReceiveVelocityUpdates(false).build("black_hole"));

    public static final RegistryObject<EntityType<StarPet>> SUN = ENTITIES.register("sun",
        () -> EntityType.Builder.of(StarPet::new, MobCategory.MISC).sized(1f, 1f)
            .clientTrackingRange(16).updateInterval(2)
            .noSave().setShouldReceiveVelocityUpdates(false).build("sun"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}