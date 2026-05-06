package hua223.calamity.register.config;

import hua223.calamity.render.CalamityOutlineRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class CalamityConfig {
    private static ForgeConfigSpec.DoubleValue zenRateAmplifier;

    private static ForgeConfigSpec.DoubleValue zenNumberAmplifier;

    private static ForgeConfigSpec.DoubleValue zergRateAmplifier;

    private static ForgeConfigSpec.IntValue zergSpawnCount;

    private static ForgeConfigSpec.DoubleValue zergNumberAmplifier;

    @OnlyIn(Dist.CLIENT)
    private static ForgeConfigSpec.IntValue detectingRadius;


    public static void register(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist.isClient()) {
            ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder()
                .comment("Client dist settings").push("client");

            client.comment("The detection radius of the Omniscience potion's highlighting effect.");
            detectingRadius = client.defineInRange("detectionRadius", 24, 1, 48);

            client.pop();
            context.registerConfig(ModConfig.Type.CLIENT, client.build());
        }

        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder()
            .comment("Server dist settings").push("server");

        server.comment("Zen potion reduces the probability of mob spawn");
        zenRateAmplifier = server.defineInRange("ProbabilityOfInfluencingSpawn", 0.4, 0, 1);

        server.comment("Zen potion reduces the number of mob spawn");
        zenNumberAmplifier = server.defineInRange("RatioOfInfluencingMaxNumber", 0.3, 0, 1);

        server.comment("The probability of successfully spawning an additional mob per zerg potion attempt");
        zergRateAmplifier = server.defineInRange("TheProbabilityOfAdditionalMobSpawns", 0.5, 0, 1);

        server.comment("The multiplier by which the Zerg potion affects the world's maximum mob cap");
        zergNumberAmplifier = server.defineInRange("MaximumMobCapMultiplier", 3d, 1d, 7d);

        server.comment("How many additional times can the Zerg potion check for spawns");
        zergSpawnCount = server.defineInRange("AdditionalSpawnAttempts", 1, 0, 7);
        server.pop();
        context.registerConfig(ModConfig.Type.SERVER, server.build());
    }

    public static void onLoadConfigInfo(ModConfig.Type type) {
        switch (type) {
            case CLIENT -> CalamityOutlineRenderer.fromConfigLoad(detectingRadius.get());
            case SERVER -> {
                CalamityConfigHelper.zenRateAmplifier = zenRateAmplifier.get();
                CalamityConfigHelper.zenNumberAmplifier = zenNumberAmplifier.get();
                CalamityConfigHelper.zergRateAmplifier = zergRateAmplifier.get();
                CalamityConfigHelper.zergSpawnCount = zergSpawnCount.get();
                CalamityConfigHelper.zergNumberAmplifier = zergNumberAmplifier.get();
            }
        }
    }
}
