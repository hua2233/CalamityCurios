package hua223.calamity.register.sounds;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.EnumRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import static hua223.calamity.register.RegisterList.SOUND_EVENTS;

public enum CalamitySounds implements EnumRegister<SoundEvent> {
    /**
     * I know it s missing a lot of sound。
     * <p>
     * This is because I forgot to register the sound when I was pre-production。
     * <p>
     * It will perhaps be slowly completed by me....
     */
    ZENITH_ATTACK("zenith_attack"),
    MAJOR_LOSS("adrenaline_major_loss"),
    ADRENALINE_ACTIVATE("adrenaline_activate"),
    NANO_ACTIVATE("nano_machines_activate"),
    FULL_ADRENALINE("full_adrenaline"),
    FULL_RAGE("full_rage"),
    RAGE_END("rage_end"),
    RAGE_ACTIVATE("rage_activate"),
    ASCENDANT_ACTIVATE("ascendant_activate"),
    ASCENDANT_OFF("ascendant_off"),
    PLASMA_BOLT("plasma_bolt"),
    EXCELSUS_RAY("excelsus_ray"),
    LARGE_WEAPON_FIRE("large_weapon_fire"),
    CURSED_DAGGER_THROW("cursed_dagger_throw"),
    NEBULA("nebula"),
    NEBULA_EXPLODE("nebula_explode"),
    PHANTOM_DEATH_RAY("phantom_death_ray"),
    LUNAR_FLARE("lunar_flare"),
    PRISM("prism"),
    AA_ACTIVATION("angelic_alliance_activation"),
    SUPREME_CALAMITAS("supreme_calamitas"),
    TERMINUS_ACTIVATE("terminus_activate"),
    TERMINUS_DEACTIVATE("terminus_deactivate"),
    TERMINUS_CHARGE("terminus_charge"),
    ENSEMBLE_OF_FOOLS("ensemble_of_fools"),
    ONSLAUGHT_OF_BEASTS("onslaught_of_beasts"),
    REIGN_OF_LORDS("reign_of_lords"),
    TRIAL_OF_THE_INSANE("trial_of_the_insane"),
    GE_ACTIVATE("gruesome_eminence_activate"),
    MAGIC("magic"),
    THROW("throw"),
    LIGHTNING("lightning-3"),
    NECROPLASMIC_BEACON("necroplasmic_beacon"),
    DARK_LIGHT("dark_light"),
    MIRROR_TELEPORT("mirror_teleport"),
    LIGHTNING_STRIKE("lightning_strike"),
    DEMON_SHADE_ENRAGE("demon_shade_enrage");

    private final RegistryObject<SoundEvent> sound;

    CalamitySounds(final String id) {
        sound = SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(CalamityCurios.ModResource(id)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    @Override
    public RegistryObject<SoundEvent> getValue() {
        return sound;
    }

    public void playSound(Entity player) {
        player.level().playSound(null, player.getX(),
            player.getY(), player.getZ(), get(), SoundSource.PLAYERS, 1f, 1f);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void playLocalSound() {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 position = minecraft.player.getEyePosition();
        minecraft.level.playLocalSound(position.x, position.y, position.z,
            get(), SoundSource.PLAYERS, 1f, 1f, false);
    }

    @OnlyIn(Dist.CLIENT)
    public String getLocationLang() {
        return "subtitle." + sound.getId().getPath();
    }
}
