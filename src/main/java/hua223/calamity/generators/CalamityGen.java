package hua223.calamity.generators;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CalamityCurios.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CalamityGen {
    private static final Map<String, List<String>> DAMAGE = new Object2ObjectOpenHashMap<>();
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new CuriosTag(output, helper));
        generator.addProvider(event.includeServer(), new GlobalLootsProvider(output));

        final Map<String, ResourceKey<DamageType>> dm = new Object2ObjectOpenHashMap<>();
        DatapackBuiltinEntriesProvider provider = generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
            output, lookupProvider, new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, context -> {
                for (Map.Entry<String, List<String>> entry : DAMAGE.entrySet()) {
                    String key = entry.getKey();
                    ResourceKey<DamageType> resource = ResourceKey.create(Registries.DAMAGE_TYPE, CalamityCurios.ModResource(key));
                    context.register(resource, new DamageType(key, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F));
                    dm.put(key, resource);
                }
            }), Set.of(CalamityCurios.MODID)));


        generator.addProvider(event.includeServer(), new DamageTypeTagsProvider(output, provider.getRegistryProvider(), CalamityCurios.MODID, helper) {
            @Override
            protected void addTags(HolderLookup.@NotNull Provider provider) {
                Map<String, TagKey<DamageType>> tagKeyMap = new Object2ObjectOpenHashMap<>();
                Multimap<TagKey<DamageType>, ResourceKey<DamageType>> multimap = HashMultimap.create();
                for (Map.Entry<String, List<String>> entry : DAMAGE.entrySet()) {
                    ResourceKey<DamageType> resource = dm.get(entry.getKey());
                    for (String s : entry.getValue()) {
                        TagKey<DamageType> tagKey = tagKeyMap.get(s);
                        if (tagKey == null) {
                            tagKey = TagKey.create(Registries.DAMAGE_TYPE, CalamityCurios.resource(s));
                            tagKeyMap.put(s, tagKey);
                        }

                        multimap.put(tagKey, resource);
                    }
                }

                for (TagKey<DamageType> tagKey : multimap.keySet()) {
                    TagAppender<DamageType> appender = tag(tagKey);
                    for (ResourceKey<DamageType> resourceKey : multimap.get(tagKey))
                        appender.add(resourceKey);
                }
            }
        });

        generator.addProvider(event.includeClient(), new ModLangGen(output, "zh_cn"));
        generator.addProvider(event.includeClient(), new ModItemModelGen(output, helper));
        generator.addProvider(event.includeClient(), new SoundGen(output, helper));
    }

    @SuppressWarnings("unchecked")
    public static AnnotationProcessor.ResolveExceptionConsumer<AnnotationProcessor> findDamageGather() {
        DamageMapping.registerUniversal(DAMAGE);
        return processor -> {
            Map<String, Object> map = processor.getAnnotationData().annotationData();
            String key = (String) map.get("key");
            Object list = map.get("tags");
            if (map.containsKey("id") || DAMAGE.containsKey(key)) {
                if (list != null) throw new UnsupportedOperationException("Duplicate registration types: " + key);
            } else DAMAGE.put(key, (List<String>) list);
            String lang = (String) map.get("zh_cn");
            if (lang != null) ModLangGen.addAdditionalEntries(Objects.requireNonNullElse((String) map.get("msg"), key), lang);
        };
    }
}
