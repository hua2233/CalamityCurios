package hua223.calamity.util.damage;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public enum DamageTags {
    NOT_TRIGGER_EVENT("not_trigger_event"),
    CALAMITY_MAGIC("calamity_magic"),
    NO_DECAY("no_decay");
    public final TagKey<DamageType> tag;

    DamageTags(String name) {
        tag = TagKey.create(Registries.DAMAGE_TYPE, CalamityCurios.ModResource(name));
    }
}
