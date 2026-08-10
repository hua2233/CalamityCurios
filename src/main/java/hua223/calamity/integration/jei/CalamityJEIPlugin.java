package hua223.calamity.integration.jei;


import com.google.common.base.CaseFormat;
import hua223.calamity.generators.ModLangGen;
import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.CalamityItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class CalamityJEIPlugin implements IModPlugin {
    public static List<Tuple<CalamityItems, MutableComponent[]>> infos = new ArrayList<>();
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return CalamityCurios.ModResource("calamity_jei_plugin");
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        for (Tuple<CalamityItems, MutableComponent[]> info : infos)
            registration.addItemStackInfo(info.getA().get().getDefaultInstance(), info.getB());
        infos = null;
    }

    @SuppressWarnings("unchecked")
    public static void findJeiInfo(AnnotationProcessor annotationProcessor) {
        final String template = "jei.calamity_curios.$.desc.%";
        annotationProcessor.addStartProcessingEntries(JeiInfo.class, processor -> {
            Map<String, Object> data = processor.getAnnotationData().annotationData();
            Class<?> clazz = processor.getDataClass();
            String simpleName = clazz.getSimpleName();

            CalamityItems items = processor.getItemEnum(data, simpleName);
            String name = data.containsKey("text") ? (String) data.get("text")
                : CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, simpleName);
            String base = template.replace("$", name);

            int lines = data.containsKey("line") ? (Integer) data.get("line") : 1;
            String[] translationKey = new String[lines];
            for (int i = 0; i < lines; i++)
                translationKey[i] = base.replace("%", String.valueOf(i)); ;

            if (processor.runData) {
                ArrayList<String> lang = (ArrayList<String>) data.get("zh_cn");
                if (lang != null) {
                    if (lang.size() != lines) CalamityCurios.LOGGER.warn("在处理{}物品时，翻译文件行({})与声明行({})不匹配，默认跳过", items.name(), lang.size(), lines);
                    else for (int i = 0; i < lang.size(); i++) ModLangGen.addAdditionalEntries(translationKey[i], lang.get(i));
                }
            }

            MutableComponent[] components = new MutableComponent[lines];
            for (int i = 0; i < components.length; i++)
                components[i] = Component.translatable(translationKey[i]);

            infos.add(new Tuple<>(items, components));
        });
    }
}
