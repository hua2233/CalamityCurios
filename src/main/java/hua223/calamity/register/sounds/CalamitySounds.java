package hua223.calamity.register.sounds;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.Items.EnumRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static hua223.calamity.register.RegisterList.SOUND_EVENTS;

public enum CalamitySounds implements EnumRegister<SoundEvent> {
    /**
     * I know it s missing a lot of sound。
     * <p>
     * This is because I forgot to register the sound when I was pre-production。
     * <p>
     * It will perhaps be slowly completed by me....
     */
    ZENITH_ATTACK("zenith_attack", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("zenith_attack"))),
    MAJOR_LOSS("adrenaline_major_loss", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("adrenaline_major_loss"))),
    ADRENALINE_ACTIVATE("adrenaline_activate", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("adrenaline_activate"))),
    NANO_ACTIVATE("nano_machines_activate", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("nano_machines_activate"))),
    FULL_ADRENALINE("full_adrenaline", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("full_adrenaline"))),
    FULL_RAGE("full_rage", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("full_rage"))),
    RAGE_END("rage_end", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("rage_end"))),
    RAGE_ACTIVATE("rage_activate", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("rage_activate"))),
    ASCENDANT_ACTIVATE("ascendant_activate", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("ascendant_activate"))),
    ASCENDANT_OFF("ascendant_off", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("ascendant_off"))),
    PLASMA_BOLT("plasma_bolt", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("plasma_bolt"))),
    EXCELSUS_RAY("excelsus_ray", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("excelsus_ray"))),
    LARGE_WEAPON_FIRE("large_weapon_fire", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("large_weapon_fire"))),
    CURSED_DAGGER_THROW("cursed_dagger_throw", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("cursed_dagger_throw"))),
    NEBULA("nebula", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("nebula"))),
    NEBULA_EXPLODE("nebula_explode", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("nebula_explode"))),
    PHANTOM_DEATH_RAY("phantom_death_ray", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("phantom_death_ray"))),
    LUNAR_FLARE("lunar_flare", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("lunar_flare"))),
    PRISM("prism", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("prism"))),
    AA_ACTIVATION("angelic_alliance_activation", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("angelic_alliance_activation"))),
    SUPREME_CALAMITAS("supreme_calamitas", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("supreme_calamitas"))),
    DEMON_SHADE_ENRAGE("demon_shade_enrage", () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource("demon_shade_enrage")));

    private final RegistryObject<SoundEvent> sound;

    CalamitySounds(String id, Supplier<SoundEvent> sounds) {
        sound = SOUND_EVENTS.register(id, sounds);
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    @Override
    public RegistryObject<SoundEvent> getValue() {
        return sound;
    }

    @OnlyIn(Dist.CLIENT)
    public String getLocationLang() {
        return "calamity_curios.sound.subtitle." + sound.getId().getPath();
    }
}
