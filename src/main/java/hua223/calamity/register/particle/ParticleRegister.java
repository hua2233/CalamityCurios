package hua223.calamity.register.particle;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ParticleRegister {
    public static RegistryObject<SimpleParticleType> GOLDEN_LEMNISCATE;
    public static RegistryObject<SimpleParticleType> ETERNITY_DUST;
    public static RegistryObject<SimpleParticleType> SAKURA;
    public static RegistryObject<Pulse.PulseType> PULSE;
    public static RegistryObject<SparkParticle.SparkType> SPARK;
    public static RegistryObject<GlowSparkParticle.GlowSparkType> GLOW_SPARK;
    public static RegistryObject<PointParticle.PointType> POINT;
    public static RegistryObject<SimpleParticleType> BLOOD;
    public static RegistryObject<ColorfulTotemType> TOTEM;
    public static RegistryObject<SimpleParticleType> ELECTRIC_EXPLOSION_RING;
    public static RegistryObject<SimpleParticleType> STORM_LIGHTNING;
    public static RegistryObject<SimpleParticleType> BALLISTIC_POISON_CLOUD;
    public static void register(IEventBus bus) {
        DeferredRegister<ParticleType<?>> PT_REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CalamityCurios.MODID);
        Supplier<SimpleParticleType> simple = () -> new SimpleParticleType(false);

        GOLDEN_LEMNISCATE = PT_REG.register("golden_lemniscate", simple);

        ETERNITY_DUST = PT_REG.register("eternity_dust", simple);

        SAKURA = PT_REG.register("sakura", simple);

        PULSE = PT_REG.register("pulse", () -> new Pulse.PulseType(false));

        SPARK = PT_REG.register("spark", () -> new SparkParticle.SparkType(false));

        GLOW_SPARK = PT_REG.register("glow_spark", () -> new GlowSparkParticle.GlowSparkType(false));

        POINT = PT_REG.register("point", () -> new PointParticle.PointType(false));

        BLOOD = PT_REG.register("blood", simple);

        TOTEM = PT_REG.register("totem", () -> new ColorfulTotemType(false));

        ELECTRIC_EXPLOSION_RING = PT_REG.register("electric_explosion_ring", simple);

        STORM_LIGHTNING = PT_REG.register("storm_lightning", simple);

        BALLISTIC_POISON_CLOUD = PT_REG.register("ballistic_poison_cloud", simple);

        PT_REG.register(bus);
    }
}
