package hua223.calamity.render.item;

import hua223.calamity.register.entity.EmptinessSpray;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmptinessSprayerLoop extends AbstractTickableSoundInstance {
    private final EmptinessSpray spray;

    public EmptinessSprayerLoop(EmptinessSpray spray) {
        super(CalamitySounds.EMPTINESS_SPRAYER_LOOP.get(), SoundSource.PLAYERS, spray.level().random);
        this.spray = spray;
        looping = true;
    }

    @Override
    public void tick() {
        if (spray.isAlive()) {
            Vec3 vec3 = spray.position();
            x = vec3.x;
            y = vec3.y;
            z = vec3.z;
        } else stop();
    }
}
