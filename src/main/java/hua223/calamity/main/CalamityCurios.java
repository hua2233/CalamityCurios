package hua223.calamity.main;

import hua223.calamity.generators.GlobalLootsProvider;
import hua223.calamity.generators.ModItemModelGen;
import hua223.calamity.generators.ModLangGen;
import hua223.calamity.generators.tag.CuriosTag;
import hua223.calamity.loots.GlobalLootModifier;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(event.includeServer(), new CuriosTag(generator, helper));
        generator.addProvider(event.includeClient(), new ModLangGen(generator, "zh_cn"));
        generator.addProvider(event.includeClient(), new ModItemModelGen(generator, helper));
        generator.addProvider(event.includeServer(), new GlobalLootsProvider(generator));
        //generator.addProvider(event.includeServer(), new ModItemTag(generator, helper));
        //generator.addProvider(event.includeServer(), new ModRecipeProvider(generator));
        //generator.addProvider(isServer, new ModBlockModelGen(generator, helper));
        //generator.addProvider(isServer, new ModLootTableProvider(generator));
        //generator.addProvider(isServer, new ModBlockTag(generator, helper));
    }

    private void build(IEventBus bus) {
        RegisterList.build(bus);
        ParticleRegister.register(bus);
        EntityDataSerializers.registerSerializer(CalamityHelp.SHORT);
        GlobalLootModifier.register(bus);
        NetMessages.registerNetPack();
        bus.addListener(this::gatherData);

        //CalamityCommands.commandInit(bus);
        //LootTableTypeCondition.register(bus);
        //CalamityCommands.commandInit(bus);
    }
}
