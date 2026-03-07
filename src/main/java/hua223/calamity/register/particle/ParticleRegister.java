package hua223.calamity.register.particle;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleRegister {
    public static RegistryObject<SimpleParticleType> GOLDEN_LEMNISCATE;
    public static RegistryObject<SimpleParticleType> ETERNITY_DUST;
    public static RegistryObject<SimpleParticleType> SAKURA;
    public static RegistryObject<Pulse.PulseType> PULSE;
    public static RegistryObject<SparkParticle.SparkType> SPARK;
    public static RegistryObject<GlowSparkParticle.GlowSparkType> GLOW_SPARK;
    public static RegistryObject<PointParticle.PointType> POINT;
    public static void register(IEventBus bus) {
        DeferredRegister<ParticleType<?>> PT_REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CalamityCurios.MODID);
        GOLDEN_LEMNISCATE = PT_REG.register("golden_lemniscate", () -> new SimpleParticleType(false));

        ETERNITY_DUST = PT_REG.register("eternity_dust", () -> new SimpleParticleType(false));

        SAKURA = PT_REG.register("sakura", () -> new SimpleParticleType(false));

        PULSE = PT_REG.register("pulse", () -> new Pulse.PulseType(false));

        SPARK = PT_REG.register("spark", () -> new SparkParticle.SparkType(false));

        GLOW_SPARK = PT_REG.register("glow_spark", () -> new GlowSparkParticle.GlowSparkType(false));

        POINT = PT_REG.register("point", () -> new PointParticle.PointType(false));

        PT_REG.register(bus);
    }
}
