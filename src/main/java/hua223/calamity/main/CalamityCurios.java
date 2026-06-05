package hua223.calamity.main;

import hua223.calamity.generators.CMLDamageTypeTagsProvider;
import hua223.calamity.generators.GlobalLootsProvider;
import hua223.calamity.generators.ModItemModelGen;
import hua223.calamity.generators.ModLangGen;
import hua223.calamity.generators.tag.CuriosTag;
import hua223.calamity.loots.GlobalLootModifier;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

// I made this mod simply because I love both games
// I'm not a professional game developer, and this was my first time working with modding
// I was not good at math and I was the only one on the development team, so i can only do my best
@Mod(CalamityCurios.MODID)
public class CalamityCurios {
    // Define mod getId in a common place for everything to reference
    public static final String MODID = "calamity_curios";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ResourceLocation ModResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation ResourceOf(String modId, String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, path);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.parse(path);
    }

    public CalamityCurios(final FMLJavaModLoadingContext context) {
        CalamityConfig.register(context);
        build(context.getModEventBus());
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        DatapackBuiltinEntriesProvider builtinEntries = generator.addProvider(event.includeClient(), new DatapackBuiltinEntriesProvider(output, lookupProvider,
            new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, CalamityDamageTypes::bootstrap), Set.of(CalamityCurios.MODID)));
        generator.addProvider(event.includeClient(), new CuriosTag(output, helper));
        generator.addProvider(event.includeClient(), new ModLangGen(output, "zh_cn"));
        generator.addProvider(event.includeClient(), new ModItemModelGen(output, helper));
        generator.addProvider(event.includeClient(), new GlobalLootsProvider(output));
        generator.addProvider(event.includeClient(), new CMLDamageTypeTagsProvider(output, builtinEntries.getRegistryProvider(), helper));
        //generator.addProvider(event.includeServer(), new ModItemTag(generator, helper));
        //generator.addProvider(event.includeServer(), new ModRecipeProvider(generator));
        //generator.addProvider(isServer, new ModBlockModelGen(generator, helper));
        //generator.addProvider(isServer, new ModLootTableProvider(generator));
        //generator.addProvider(isServer, new ModBlockTag(generator, helper));
    }

    private void build(IEventBus bus) {
        RegisterList.build(bus);
        ParticleRegister.register(bus);
        GlobalLootModifier.register(bus);
        NetMessages.registerNetPack();
        bus.addListener(this::gatherData);
        //CalamityCommands.commandInit(bus);
        //LootTableTypeCondition.register(bus);
    }
}
