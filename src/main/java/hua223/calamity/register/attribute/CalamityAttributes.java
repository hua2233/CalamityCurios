package hua223.calamity.register.attribute;

import hua223.calamity.register.Items.EnumRegister;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

import static hua223.calamity.register.RegisterList.ATTRIBUTES;

public enum CalamityAttributes implements EnumRegister<Attribute> {
    CRITICAL_STRIKE_CHANCE("critical_strike_chance", 1, 1, 2),
    CLOSE_CRITICAL_STRIKE_CHANCE("close_critical_strike_chance", 1, 1, 2),
    FAR_CRITICAL_STRIKE_CHANCE("far_critical_strike_chance", 1, 1, 2),
    ARMOR_PENETRATE("armor_penetrate", 0, 0, 1024),
    FAR_ATTACK("far_attack", 1, 1, 10),
    AMMUNITION_ADD("ammunition_add", 1, 1, 2),
    INJURY_OFFSET("injury_offset", 1, 1, 2),
    DAMAGE_UP("damage_up", 1, 1, 10),
    CLOSE_RANGE("close_range", 1, 1, 10),
    MAGIC_REDUCTION("magic_reduction", 1, 0.15, 1.85);

    private final RegistryObject<Attribute> value;

    CalamityAttributes(String name, double defaultValue, double min, double max) {
        this.value = ATTRIBUTES.register(name, () ->
            new RangedAttribute("attribute.calamity_curios." + name, defaultValue, min, max));
    }

    public static void build(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }

    public static Attribute[] getAll() {
        return Arrays.stream(CalamityAttributes.values()).map(EnumRegister::get).toArray(Attribute[]::new);
    }

    @Override
    public RegistryObject<Attribute> getValue() {
        return value;
    }
}
