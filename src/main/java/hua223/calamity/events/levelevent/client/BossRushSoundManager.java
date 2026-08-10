package hua223.calamity.events.levelevent.client;

import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class BossRushSoundManager {
    private final ClientRushEvent event;
    private SoundInstance inPlaySound;
    private BossRushSound backgroundMusic;
    private final SoundManager manager = Minecraft.getInstance().getSoundManager();

    private ArrayList<Tuple<CalamitySounds, Integer>> backgroundMusicSet;

    public BossRushSoundManager(ClientRushEvent event) {
        this.event = event;
    }

    private void prepareMusicResources() {
        backgroundMusicSet = new ArrayList<>();
        backgroundMusicSet.add(new Tuple<>(CalamitySounds.TRIAL_OF_THE_INSANE, 4480));
        backgroundMusicSet.add(new Tuple<>(CalamitySounds.REIGN_OF_LORDS, 3480));
        backgroundMusicSet.add(new Tuple<>(CalamitySounds.ONSLAUGHT_OF_BEASTS, 3480));
        backgroundMusicSet.add(new Tuple<>(CalamitySounds.ENSEMBLE_OF_FOOLS, 4480));
    }

    @SuppressWarnings("ConstantConditions")
    void fromDataPlaySound(CompoundTag data) {
        String type = data.getString("sound");
        if (type.equals("stop")) stopInPlaySound();
        else if (type.equals("replay")) {
            if (backgroundMusic != null) manager.play(backgroundMusic);
        } else {
            CalamitySounds sound = CalamitySounds.valueOf(type);
            inPlaySound = SimpleSoundInstance.forAmbientAddition(sound.get());
            manager.play(inPlaySound);
        }
    }

    void stopInPlaySound() {
        if (inPlaySound != null) {
            manager.stop(inPlaySound);
            inPlaySound = null;
        }
    }

    void startPlayBackgroundMusic() {
        if (backgroundMusicSet == null) prepareMusicResources();
        if (!backgroundMusicSet.isEmpty()) {
            Tuple<CalamitySounds, Integer> tuple = backgroundMusicSet.remove(backgroundMusicSet.size() - 1);
            backgroundMusic = new BossRushSound(tuple.getA().get(), tuple.getB());
            manager.play(backgroundMusic);
        }
    }

    void fadeOutMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.fadeDirection = false;
            backgroundMusic.playNextState = true;
        }
    }

    private class BossRushSound extends AbstractTickableSoundInstance {
        private boolean fadeDirection = true;
        private int fade;
        private int tick;
        private final int fadeOutTick;
        private boolean playNextState;

        public BossRushSound(SoundEvent soundEvent, int totalDuration) {
            super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            looping = true;
            delay = 0;
            volume = .000001f;
            relative = true;
            fadeOutTick = totalDuration - 30;
        }

        @Override
        public void tick() {
            if (event.isStop())  fadeDirection = false;

            tick++;
            if (fadeDirection) {
               if (fade < 30) volume = Mth.clamp((fade += 1) / 30f, 0.0F, 1.0F);
               else if (tick == fadeOutTick) fadeDirection = false;
            } else {
                volume = Mth.clamp((fade -= 1) / 30f, 0.0F, 1.0F);
                if (fade == 0) {
                    if (event.isStop() || playNextState){
                        stop();
                    } else {
                        tick = 0;
                        fadeDirection = true;
                    }
                }
            }
        }
    }
}
