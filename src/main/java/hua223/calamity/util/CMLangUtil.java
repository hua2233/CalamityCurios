package hua223.calamity.util;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CMLangUtil {
    private static final String TOOLTIP_PREFIX = "tooltip.calamity_curios.";
    private static final String LINE_PREFIX = ".line";
    private static final String ITEM_GROUP_PREFIX = "itemGroup.";
    private static final String ATTRIBUTE_PREFIX = "attribute.calamity_curios.";
    private static final String EFFECT_TOOLTIP_PREFIX = "effect.tooltip.";
    //private static final String DESC = ".desc";
    private static final String DEATH = "death.attack.";
    private static final String DEATH_PLAYER = ".player";

    public static String getCommonText(String name, int line) {
        return TOOLTIP_PREFIX + name + LINE_PREFIX + line;
    }

    public static String getCommonText(String name) {
        return TOOLTIP_PREFIX + name;
    }

    public static String getDeath(String type) {
        return DEATH + type;
    }

    public static String getEffectText(String name) {
        return EFFECT_TOOLTIP_PREFIX + name;
    }

    public static String getDeathPlayer(String type) {
        return DEATH + type + DEATH_PLAYER;
    }

    public static String getAttribute(String name) {
        return ATTRIBUTE_PREFIX + name;
    }

    public static MutableComponent getTranslatable(String name, int line) {
        return Component.translatable(TOOLTIP_PREFIX + name + LINE_PREFIX + line);
    }

//    public static MutableComponent advancementsTitle(String title) {
//        return Component.translatable("advancements.calamity_curios.title." + title);
//    }
//
//    public static MutableComponent advancementsDescription(String description) {
//        return Component.translatable("advancements.calamity_curios.description." + description);
//    }

    public static Component blankLine() {
        return CommonComponents.EMPTY;
    }

    public static MutableComponent getTranslatable(String name) {
        return Component.translatable(TOOLTIP_PREFIX + name);
    }

    public static MutableComponent getView() {
        return Component.translatable(TOOLTIP_PREFIX + "view");
    }

    public static MutableComponent getEffectTranslatable(String name) {
        return Component.translatable(EFFECT_TOOLTIP_PREFIX + name);
    }

    public static MutableComponent getDynamic(String name, int line, Object... args) {
        return Component.translatable(TOOLTIP_PREFIX + name + LINE_PREFIX + line, args);
    }

    public static MutableComponent getDynamic(String name, Object args) {
        return Component.translatable(TOOLTIP_PREFIX + name, args);
    }

//    public static String getRecord(Item item) {
//        return item.getDescriptionId() + DESC;
//    }

    public static String groupKey(CreativeModeTab tab) {
        return ITEM_GROUP_PREFIX + tab.getRecipeFolderName();
    }
}
