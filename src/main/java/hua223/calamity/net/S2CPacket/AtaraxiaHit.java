package hua223.calamity.net.S2CPacket;

import com.mojang.math.Vector4f;
import hua223.calamity.register.particle.GlowSparkParticle;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.register.particle.Pulse;
import hua223.calamity.register.particle.SparkParticle;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;

public class AtaraxiaHit extends S2C {
    private final int id;

    public AtaraxiaHit(LivingEntity entity) {
        id = entity.getId();
    }

    @OnlyIn(Dist.CLIENT)
    public AtaraxiaHit(FriendlyByteBuf buf) {
        id = buf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        //This is only triggered locally on the client side, because the count might be too high
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity target = level.getEntity(id);
            if (target != null) {
                ParticleOptions type = ParticleRegister.SAKURA.get();
                Random random = new Random();
                double x = target.getX();
                double y = target.getY() + target.getBbHeight() / 2f;
                double z = target.getZ();
                Vector4f color = RenderUtil.black();
                level.playLocalSound(x, y, z, CalamitySounds.CURSED_DAGGER_THROW.get(), SoundSource.PLAYERS, 1f, 1f, false);

                for (int i = 0; i < 10; i++) {
                    Vector2d velocity = new Vector2d(1, 1).rotatedByRandom(random, Vector2d.ZERO, 100, true)
                        .mul(random.nextFloat(0.3f, 1.2f)).mul(random.nextFloat(0.2f, 1f));
                    level.addParticle(type, x, y, z, velocity.x, velocity.y, 0);
                }

                float zOffset = 0.001f;
                for (float k = 0f; k < 3; k++) {
                    float colorRando = random.nextFloat();
                    int partLifetime = random.nextInt(6, 9);
                    float scale = random.nextFloat(3f, 3.5f);
                    Vector2d spawnPos = Vector2d.nextVector2Circular(1f, 1f, random, 0.1f, 0.16f).mul(k + 1);

                    level.addParticle(new Pulse.PulseOptions(0.6f, scale, random.nextFloat(-10, 10),
                        1, 1, RenderUtil.interpolateColor(RenderUtil.DARK_ORCHID, RenderUtil.INDIAN_RED, colorRando, color),
                        partLifetime), x + spawnPos.x, y + spawnPos.y, z + zOffset, 0, 0, 0);
                    zOffset += 0.001f;
                }

                for (int k = 0; k < 5; k++) {
                    Vector2d velocity = new Vector2d(0.6, 0.6).rotatedByRandom(random,
                        Vector2d.ZERO, 100, true).mul(random.nextFloat(0.4f, 0.8f));

                    float colorRando = random.nextFloat();
                    level.addParticle(new SparkParticle.SparkOptions(random.nextFloat(.5f, .75f),
                            16, RenderUtil.interpolateColor(RenderUtil.DARK_ORCHID, RenderUtil.INDIAN_RED, colorRando, color)),
                        x + velocity.x, y + velocity.y, z, velocity.x, velocity.y, 0);
                }

                //Elongated spark particles, resembling splashing Mars
                for (int k = 0; k < 10; k++) {
                    Vector2d velocity = new Vector2d(0.6, 0.6).rotatedByRandom(random,
                        Vector2d.ZERO, 100, true).mul(random.nextFloat(0.2f, 0.8f));

                    float colorRando = random.nextFloat();

                    level.addParticle(new GlowSparkParticle.GlowSparkOptions(random.nextFloat(.1f, .14f),
                            6, RenderUtil.interpolateColor(RenderUtil.DARK_ORCHID, RenderUtil.INDIAN_RED, colorRando, color),
                            2.2f, 0.9f, true, true),
                        x + velocity.x, y + velocity.y, z, velocity.x, velocity.y, 0);
                }

                //Generate a circular bloom of sakura particles with 6-wave undulation, creating a flower-like spreading effect
                //Uses sinusoidal radial modulation to simulate natural petal distribution
                int flowerPetalCount = 6;
                float thetaDelta = new Vector2d(0.2, 0.2).rotatedByRandom(random, Vector2d.ZERO, 100, true).toRotation();
                float weaveDistanceMin = 0.025f;
                float weaveDistanceOutwardMax = 0.5f;
                float weaveDistanceInner = 0.025f;

                for (float theta = 0f; theta < Mth.TWO_PI; theta += 0.03f) {
                    Vector2d velocity = Vector2d.toRotationVector2(theta).mul(
                        weaveDistanceMin +
                            // The 0.5 in here is to prevent the petal from looping back into itself. With a 0.5 addition, it is perfect, coming back to (0,0)
                            // instead of weaving backwards.
                            (float) (Math.sin(thetaDelta + theta * flowerPetalCount) + 0.5f + weaveDistanceInner) * weaveDistanceOutwardMax);
                    level.addParticle(type, x, y, z, velocity.x, velocity.y, 0);
                }
            }
        }
    }
}
