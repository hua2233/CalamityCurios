package hua223.calamity.generators;

import java.util.List;
import java.util.Map;

public interface DamageMapping {
    String NTE = "calamity_curios:not_trigger_event";
    String CM_MAGIC = "calamity_curios:magic";
    String BYPASSES_RESISTANCE = "bypasses_resistance";
    String BYPASSES_ARMOR = "bypasses_armor";
    String BYPASSES_ENCHANTMENTS = "bypasses_enchantments";
    String BYPASSES_EFFECTS = "bypasses_effects";
    String BYPASSES_COOLDOWN = "bypasses_cooldown";
    String IS_FIRE = "is_fire";
    String NO_IMPACT = "no_impact";
    String BYPASSES_SHIELD = "bypasses_shield";
    String AVOIDS_GUARDIAN_THORNS = "avoids_guardian_thorns";
    String IS_PROJ = "is_projectile";

    //DamageType
    String BLEEDING = "bleeding";
    String MAGIC_FIRE = "magic_fire";
    String SPUTTERING = "sputtering";
    String MAGIC_PROJECTILE = "magic_projectile";

    String GENERIC_KILL = "generic_kill";

    static void registerUniversal(Map<String, List<String>> map) {
        map.put(BLEEDING, List.of(BYPASSES_SHIELD, BYPASSES_COOLDOWN, NO_IMPACT, AVOIDS_GUARDIAN_THORNS, CM_MAGIC, NTE));
        map.put(MAGIC_FIRE, List.of(IS_FIRE, BYPASSES_ARMOR, BYPASSES_ENCHANTMENTS, AVOIDS_GUARDIAN_THORNS, CM_MAGIC));
        map.put(SPUTTERING, List.of(NTE, BYPASSES_COOLDOWN, AVOIDS_GUARDIAN_THORNS, BYPASSES_SHIELD));
        map.put(MAGIC_PROJECTILE, List.of(IS_PROJ, BYPASSES_ARMOR, AVOIDS_GUARDIAN_THORNS, CM_MAGIC));
    }
}
