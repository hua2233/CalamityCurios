package hua223.calamity.util.damage;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public enum CalamityDamageTypes {
    ABYSS("sink", ChatFormatting.BLUE, DamageTags.NOT_TRIGGER_EVENT.tag),

    SULFUR_FIRE("sulfur_fire", ChatFormatting.DARK_RED, DamageTypeTags.IS_FIRE, DamageTypeTags.BYPASSES_ARMOR,
        DamageTypeTags.BYPASSES_EFFECTS, DamageTypeTags.BYPASSES_ENCHANTMENTS),

    PLAGUE("plague", ChatFormatting.DARK_GREEN, DamageTags.NOT_TRIGGER_EVENT.tag,
        DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.BYPASSES_ENCHANTMENTS),

    BLOOD_GOD("blood_god", ChatFormatting.DARK_RED, DamageTags.NOT_TRIGGER_EVENT.tag,
        DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.BYPASSES_COOLDOWN, DamageTypeTags.BYPASSES_RESISTANCE, DamageTypeTags.NO_IMPACT),

    ASTRAL_INJECTION("astral_injection", ChatFormatting.AQUA, DamageTags.NOT_TRIGGER_EVENT.tag, DamageTypeTags.BYPASSES_ARMOR),

    ASTR_EROSION("astr_erosion", ChatFormatting.AQUA, DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.BYPASSES_ENCHANTMENTS,
        DamageTypeTags.BYPASSES_EFFECTS, DamageTypeTags.BYPASSES_COOLDOWN, DamageTypeTags.BYPASSES_RESISTANCE,
        DamageTags.NO_DECAY.tag, DamageTags.NOT_TRIGGER_EVENT.tag),

    BLAZING_CORE("blazing_core", ChatFormatting.RED, DamageTags.NO_DECAY.tag),

    UNIVERSE_SPLITTER_BEAM("universe_splitter_beam", ChatFormatting.LIGHT_PURPLE, DamageTags.NO_DECAY.tag, DamageTypeTags.BYPASSES_ARMOR),

    ETERNITY_HEX("eternity", ChatFormatting.LIGHT_PURPLE, DamageTags.NO_DECAY.tag,
        DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.BYPASSES_COOLDOWN, DamageTags.CALAMITY_MAGIC.tag),

    HOLY_FLAMES("holy_flames", ChatFormatting.GOLD, DamageTypeTags.BYPASSES_ARMOR,
        DamageTypeTags.BYPASSES_ENCHANTMENTS, DamageTypeTags.BYPASSES_EFFECTS,
        DamageTypeTags.BYPASSES_COOLDOWN, DamageTypeTags.BYPASSES_RESISTANCE,
        DamageTypeTags.IS_FIRE),

    DRAGON_FIRE("dragon_fire", ChatFormatting.GOLD, DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.IS_FIRE),

    FORESEE("foresee", ChatFormatting.AQUA, DamageTags.NOT_TRIGGER_EVENT.tag, DamageTags.NO_DECAY.tag, DamageTypeTags.BYPASSES_COOLDOWN),

    POLARIZER_HURT("polarizer_hurt", ChatFormatting.AQUA, DamageTypeTags.BYPASSES_ARMOR, DamageTags.NOT_TRIGGER_EVENT.tag),

    MAGIC_PROJECTILE("magic_projectile", ChatFormatting.AQUA,
        DamageTypeTags.IS_PROJECTILE, DamageTypeTags.BYPASSES_ARMOR, DamageTags.NOT_TRIGGER_EVENT.tag),

    PRISM("prism", ChatFormatting.GOLD, DamageTypeTags.BYPASSES_ARMOR,
        DamageTypeTags.BYPASSES_COOLDOWN, DamageTags.CALAMITY_MAGIC.tag);

    public final ResourceKey<DamageType> type;
    @SuppressWarnings("rawtypes")
    public final TagKey[] tags;
    public final ChatFormatting style;

    @SafeVarargs
    CalamityDamageTypes(String name, ChatFormatting formatting, TagKey<DamageType>... tags) {
        this.tags = tags.length > 0 ? tags : null;
        type = ResourceKey.create(Registries.DAMAGE_TYPE, CalamityCurios.ModResource(name));
        style = formatting;
    }

    public static void bootstrap(BootstapContext<DamageType> context) {
        for (CalamityDamageTypes types : CalamityDamageTypes.values())
            context.register(types.type, new DamageType(types.type.location().getPath(),
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F));
    }
}
