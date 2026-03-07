package hua223.calamity.register.gui;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import static hua223.calamity.register.RegisterList.CALAMITY_ENCHANTMENT;

public class RegisterMenuType {
    public static void build(IEventBus bus) {
        CALAMITY_ENCHANTMENT.register(bus);
    }

    public static final RegistryObject<MenuType<CalamityCurseMenu>> CALAMITY_CURES =
        CALAMITY_ENCHANTMENT.register("calamity_curse_enchantment", () -> IForgeMenuType.create(CalamityCurseMenu::new));
}
